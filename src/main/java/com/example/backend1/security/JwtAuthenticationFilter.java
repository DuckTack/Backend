package com.example.backend1.security;

import com.example.backend1.common.ApiResponse;
import com.example.backend1.common.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService userDetailsService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public JwtAuthenticationFilter(
          JwtTokenProvider jwtTokenProvider,
          CustomUserDetailsService userDetailsService
  ) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(
          HttpServletRequest request,
          HttpServletResponse response,
          FilterChain filterChain
  ) throws ServletException, IOException {

    String auth = request.getHeader("Authorization");
    System.out.println("AUTH HEADER = " + auth);

    if (auth != null && auth.startsWith("Bearer ")) {
      try {
        String token = auth.substring(7);
        var claims = jwtTokenProvider.parseClaims(token);
        String username = claims.getSubject();

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        var authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

      } catch (Exception e) {
        response.setStatus(ErrorCode.AUTH_FAILED.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        ApiResponse.error(
                                ErrorCode.AUTH_FAILED.code(),
                                ErrorCode.AUTH_FAILED.message()
                        )
                )
        );
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
