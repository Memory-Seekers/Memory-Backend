package lookIT.lookITspring.config;

import lombok.RequiredArgsConstructor;
import lookIT.lookITspring.security.JwtAuthenticationFilter;
import lookIT.lookITspring.security.JwtProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
public class JwtSecurityConfig extends
	SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
	private final JwtProvider jwtProvider;
	private final RedisTemplate redisTemplate;

	@Override
	public void configure(HttpSecurity http) {
		JwtAuthenticationFilter customFilter = new JwtAuthenticationFilter(jwtProvider, redisTemplate);
		http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
	}
}
