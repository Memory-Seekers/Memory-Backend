package lookIT.lookITspring.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtAuthenticationProvider;
    private final RedisTemplate redisTemplate;
//    public JwtAuthenticationFilter(JwtProvider provider) {
//        jwtAuthenticationProvider = provider;
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        String token = jwtAuthenticationProvider.resolveToken(request);

        if (token != null && jwtAuthenticationProvider.validateToken(token)) {
            String isLogout = (String)redisTemplate.opsForValue().get(token);

            if (ObjectUtils.isEmpty(isLogout)) {
                Authentication authentication = jwtAuthenticationProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
