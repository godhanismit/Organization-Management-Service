package BackendInternAssignment.example.Organization.Management.Service.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final AdminUserDetailsService adminUserDetailsService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   AdminUserDetailsService adminUserDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.adminUserDetailsService = adminUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtTokenService.parseClaims(token);
                String email = claims.getSubject();
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    AdminUserDetails userDetails = (AdminUserDetails) adminUserDetailsService.loadUserByUsername(email);
                    if (claims.getExpiration().toInstant().isAfter(java.time.Instant.now())) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception ignored) {
                // let SecurityConfig handle unauthorized access downstream
            }
        }
        filterChain.doFilter(request, response);
    }
}

