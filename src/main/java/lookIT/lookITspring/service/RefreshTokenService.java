package lookIT.lookITspring.service;

import lombok.RequiredArgsConstructor;
import lookIT.lookITspring.dto.TokenRefreshResponseDto;
import lookIT.lookITspring.entity.RefreshToken;
import lookIT.lookITspring.exception.TokenRefreshException;
import lookIT.lookITspring.repository.RefreshTokenRepository;
import lookIT.lookITspring.repository.UserRepository;
import lookIT.lookITspring.security.JwtProvider;

@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	private final JwtProvider jwtProvider;

	public RefreshToken createRefreshToken(Long userId) {

		RefreshToken refreshToken = RefreshToken.builder()
			.user(userRepository.findById(userId).get())
			.token(jwtProvider.createRefreshToken(userId)).build();

		RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);
		return savedRefreshToken;
	}

	public RefreshToken verifyExpiration(RefreshToken token) {
		if (jwtProvider.getExpiration(token.getToken()) < 0) {
			refreshTokenRepository.delete(token);
			throw new TokenRefreshException(token.getToken(), "Refresh token is expired. Please make a new login request.");
		}
		return token;
	}

	public TokenRefreshResponseDto reissueAccessToken(String refreshToken){
		return refreshTokenRepository.findByToken(refreshToken)
			.map(this::verifyExpiration)
			.map(RefreshToken::getUser)
			.map(user -> {
				String token = jwtProvider.createAccessToken(user.getUserId());
				return new TokenRefreshResponseDto(token, refreshToken);
			})
			.orElseThrow(() -> new TokenRefreshException(refreshToken, "Refresh token is not in database. Please make a new login request."));
	}

}
