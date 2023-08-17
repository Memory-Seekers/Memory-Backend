package lookIT.lookITspring.service;

import java.util.Map;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lookIT.lookITspring.dto.JwtResponseDto;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.entity.RefreshToken;
import lookIT.lookITspring.entity.User;
import lookIT.lookITspring.repository.RefreshTokenRepository;
import lookIT.lookITspring.repository.UserRepository;
import lookIT.lookITspring.security.JwtProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public boolean join(UserJoinRequestDto requestDto) {

        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        User user = userRepository.save(requestDto.toEntity());
        user.encodePassword(passwordEncoder);

        return true;
    }

    public JwtResponseDto login(Map<String, String> members) {
        User user = userRepository.findByEmail(members.get("email"))
            .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 Email 입니다."));

        String password = members.get("password");
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getUserId());
        RefreshToken refreshToken =  refreshTokenService.createRefreshToken(user.getUserId());

        return new JwtResponseDto(accessToken, refreshToken.getToken());
    }

    public boolean checkIdDuplicate(String tagId) {
        Optional<User> optionalMember = userRepository.findByTagId(tagId);
        if (optionalMember.isEmpty())
            return true;
        else
            return false;
    }

    public boolean logout(String token) {
        Long userId = jwtProvider.getUserId(token);
        refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
        return true;
    }

    public String emailConfirm(String email) throws Exception{
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("해당 이메일로 가입된 유저가 없습니다."));;

        return emailService.sendSimpleMessage(email);
    }

    public String emailConfirmJoin(String email) throws Exception {
        return emailService.sendSimpleMessage2(email);
    }

    public boolean regeneratePassword(Map<String, String> request) {
        User user = userRepository.findByEmail(request.get("email")).get();
        user.update(request.get("password"));
        user.encodePassword(passwordEncoder);
        return true;
    }

}