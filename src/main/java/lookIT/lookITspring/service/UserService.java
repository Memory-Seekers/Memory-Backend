package lookIT.lookITspring.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.entity.User;
import lookIT.lookITspring.repository.UserRepository;
import lookIT.lookITspring.security.JwtProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate redisTemplate;
    private final EmailService emailService;

    @Transactional
    public boolean join(UserJoinRequestDto requestDto) throws Exception {

        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        User user = userRepository.save(requestDto.toEntity());
        user.encodePassword(passwordEncoder);

        return true;
    }

    public String login(Map<String, String> members) {
        User user = userRepository.findByEmail(members.get("email"))
            .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 Email 입니다."));

        String password = members.get("password");
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }
        return jwtProvider.createToken(user.getUserId());
    }

    public boolean checkIdDuplicate(String tagId) {
        Optional<User> optionalMember = userRepository.findByTagId(tagId);
        try {
            User user = optionalMember.get();
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public boolean logout(String token) {
        Long expiration = jwtProvider.getExpiration(token);

        redisTemplate.opsForValue()
            .set(token, "logout", expiration, TimeUnit.MILLISECONDS);
        return true;
    }

    public String emailConfirm(String email) throws Exception{
        try {
            User user = userRepository.findByEmail(email).get();
        } catch (Exception e) {
            return "해당 이메일로 가입된 유저가 없습니다.";
        }

        String confirm = emailService.sendSimpleMessage(email);
        return confirm;
    }

    public String emailConfirmJoin(String email) throws Exception {
        String confirm = emailService.sendSimpleMessage2(email);
        return confirm;
    }

    public boolean regeneratePassword(Map<String, String> request) {
        User user = userRepository.findByEmail(request.get("email")).get();
        user.update(request.get("password"));
        user.encodePassword(passwordEncoder);
        return true;
    }
}