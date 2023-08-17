package lookIT.lookITspring.dto;

import lombok.Data;

@Data
public class TokenRefreshResponseDto {

	private String accessToken;
	private String refreshToken;

	public TokenRefreshResponseDto(String accessToken, String refreshToken) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}

}
