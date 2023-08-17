package lookIT.lookITspring.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.jsonwebtoken.ExpiredJwtException;
import java.util.HashMap;
import javax.transaction.Transactional;
import lookIT.lookITspring.dto.JwtResponseDto;
import lookIT.lookITspring.dto.TokenRefreshResponseDto;
import lookIT.lookITspring.dto.UserJoinRequestDto;
import lookIT.lookITspring.entity.RefreshToken;
import lookIT.lookITspring.entity.User;
import lookIT.lookITspring.exception.TokenRefreshException;
import lookIT.lookITspring.repository.RefreshTokenRepository;
import lookIT.lookITspring.repository.UserRepository;
import lookIT.lookITspring.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
public class RefreshTokenServiceTest {

	@Autowired UserService userService;
	@Autowired RefreshTokenService refreshTokenService;
	@Autowired JwtProvider jwtProvider;
	@Autowired UserRepository userRepository;
	@Autowired RefreshTokenRepository refreshTokenRepository;

	String tagId ="test";
	String email ="test@gmail.com";
	String password ="memoryRecord123!";
	String nickName ="test";

	JwtResponseDto tokens;

	@BeforeEach
	void setUp() throws Exception {
		UserJoinRequestDto user = new UserJoinRequestDto(tagId, email, password, nickName);
		userService.join(user);

		HashMap<String, String> user1 = new HashMap<>();
		user1.put("email", email);
		user1.put("password", password);

		tokens = userService.login(user1);
	}

	@Test
	@DisplayName("액세스 토큰 재발급 성공")
	public void reissueAccessTokenSuccess() throws Exception {
		//When
		TokenRefreshResponseDto issuedTokens = refreshTokenService.reissueAccessToken(tokens.getRefreshToken());

		//Then
		User user = userRepository.findByEmail(email).get();
		assertEquals(jwtProvider.createAccessToken(user.getUserId()), issuedTokens.getAccessToken());
		assertEquals(tokens.getRefreshToken(), issuedTokens.getRefreshToken());
	}

	@Test
	@DisplayName("액세스 토큰 재발급 실패: 리프레시 토큰의 유효기간이 지났을 경우")
	public void reissueAccessTokenFail1() throws Exception {
		//Given
		RefreshToken refreshToken = refreshTokenRepository.findByToken(tokens.getRefreshToken()).get();
		String expiredRefreshToken = jwtProvider.createExpiredRefreshToken();
		refreshToken.updateToken(expiredRefreshToken);

		//When
		ExpiredJwtException e = assertThrows(ExpiredJwtException.class,
			() -> refreshTokenService.reissueAccessToken(refreshToken.getToken()));
	}

	@Test
	@DisplayName("액세스 토큰 재발급 실패: 로그아웃된 리프레시 토큰을 사용할 경우")
	public void reissueAccessTokenFail2() throws Exception {
		//Given
		userService.logout(tokens.getAccessToken());

		//When
		TokenRefreshException e = assertThrows(TokenRefreshException.class,
			() -> refreshTokenService.reissueAccessToken(tokens.getRefreshToken()));
		assertThat(e.getMessage()).isEqualTo(String.format("Failed for [%s]: %s", tokens.getRefreshToken(), "Refresh token is not in database. Please make a new login request."));
	}

}
