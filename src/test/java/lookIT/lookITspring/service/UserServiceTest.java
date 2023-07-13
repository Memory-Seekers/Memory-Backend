package lookIT.lookITspring.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import javax.transaction.Transactional;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.entity.User;
import lookIT.lookITspring.repository.UserRepository;
import lookIT.lookITspring.security.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ObjectUtils;

@SpringBootTest
@Transactional
public class UserServiceTest {

	@Autowired UserService userService;
	@Autowired UserRepository userRepository;
	@Autowired JwtProvider jwtProvider;
	@Autowired RedisTemplate redisTemplate;
	@Autowired PasswordEncoder passwordEncoder;

	@Test
	@DisplayName("회원가입_성공")
	public void joinSuccess() throws Exception {
		//Given
		String tagId ="lookIt-test1";
		String email ="thelookit06@gmail.com";
		String password ="memoryRecord123!";
		String nickName ="lookIt-test";

		UserJoinRequestDto user1 = new UserJoinRequestDto(tagId, email, password, nickName);

		//When
		userService.join(user1);

		//Then
		User findUser = userRepository.findByEmail(email).get();
		assertEquals(tagId, findUser.getTagId());
	}

	@Test
	@DisplayName("회원가입_중복회원예외")
	public void joinFail1() throws Exception {
		//Given
		String tagId ="lookIt-test1";
		String email ="thelookit06@gmail.com";
		String password ="memoryRecord123!";
		String nickName ="lookIt-test";

		UserJoinRequestDto user1 = new UserJoinRequestDto(tagId, email, password, nickName);
		UserJoinRequestDto user2 =  new UserJoinRequestDto(tagId, email, password, nickName);

		//When
		userService.join(user1);
		IllegalStateException e = assertThrows(IllegalStateException.class,
			() -> userService.join(user2));
		assertThat(e.getMessage()).isEqualTo("이미 존재하는 이메일입니다.");
	}

	@Test
	@DisplayName("로그인_성공")
	public void loginSuccess() throws Exception {
		//Given
		String email ="whitez1502@gmail.com";
		String password ="memoryRecord123!";

		HashMap<String, String> user1 = new HashMap<>();
		user1.put("email", email);
		user1.put("password", password);

		//When
		String token1 = userService.login(user1);

		//Then
		Long userId1 = jwtProvider.getUserId(token1);
		User findUser = userRepository.findById(userId1).get();
		assertEquals(email, findUser.getEmail());
	}

	@Test
	@DisplayName("로그인_가입되지않은이메일예외")
	public void loginFail1() throws Exception {
		//Given
		String email ="1502@gmail.com";
		String password ="memoryRecord123!";

		HashMap<String, String> user1 = new HashMap<>();
		user1.put("email", email);
		user1.put("password", password);

		//When
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
			() -> userService.login(user1));
		assertThat(e.getMessage()).isEqualTo("가입되지 않은 Email 입니다.");
	}

	@Test
	@DisplayName("로그인_일치하지않는비밀번호예외")
	public void loginFail2() throws Exception {
		//Given
		String email ="whitez1502@gmail.com";
		String password ="memoryRecord123";

		HashMap<String, String> user1 = new HashMap<>();
		user1.put("email", email);
		user1.put("password", password);

		//When
		BadCredentialsException e = assertThrows(BadCredentialsException.class,
			() -> userService.login(user1));
		assertThat(e.getMessage()).isEqualTo("비밀번호가 일치하지 않습니다.");
	}

	@Test
	@DisplayName("로그아웃_성공")
	public void logoutSuccess() throws Exception {
		//Given
		String email ="whitez1502@gmail.com";
		String password ="memoryRecord123!";

		HashMap<String, String> user1 = new HashMap<>();
		user1.put("email", email);
		user1.put("password", password);

		String token1 = userService.login(user1);

		//When
		userService.logout(token1);

		//Then
		String isLogout = (String)redisTemplate.opsForValue().get(token1);
		assertFalse(ObjectUtils.isEmpty(isLogout));
	}

	@Test
	@DisplayName("중복아이디확인_성공")
	public void checkIdDuplicateSuccess() throws Exception {
		//Given
		String newTagId = "test1";
		String oldTagId = "whitez1502";

		//When
		assertTrue(userService.checkIdDuplicate(newTagId));
		assertFalse(userService.checkIdDuplicate(oldTagId));
	}

	@Test
	@DisplayName("비밀번호이메일인증_없는이메일예외")
	public void emailConfirmFail() throws Exception {
		//Given
		String email ="test@gmail.com";

		//When
		IllegalStateException e = assertThrows(IllegalStateException.class,
			() -> userService.emailConfirm(email));
		assertThat(e.getMessage()).isEqualTo("해당 이메일로 가입된 유저가 없습니다.");
	}

	@Test
	@DisplayName("비밀번호재생성_성공")
	public void regeneratePasswordSuccess() throws Exception {
		//Given
		String email ="whitez1502@gmail.com";
		String password ="modifiedPassword";

		HashMap<String, String> user1 = new HashMap<>();
		user1.put("email", email);
		user1.put("password", password);

		//When
		userService.regeneratePassword(user1);

		//Then
		User findUser = userRepository.findByEmail(email).get();
		assertTrue(passwordEncoder.matches(password, findUser.getPassword()));
	}
}
