package com.ga.company_service.security;

import com.ga.company_service.client.AuthClient;
import com.ga.company_service.model.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class AuthFilter extends OncePerRequestFilter {
    @Autowired
    private AuthClient authClient;

    private String parseJwt(HttpServletRequest request){
        String headerAuth= request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")){
            return headerAuth.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        try {
            String jwt= parseJwt(request);
            if (jwt !=null){
                UserResponse user= authClient.getCurrentUser(jwt);

                if (user != null && user.getId() != null){
                    UsernamePasswordAuthenticationToken authentication= new UsernamePasswordAuthenticationToken(
                            user, null, Collections.emptyList());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e){
            SecurityContextHolder.clearContext();
            logger.error("Invalid authentication token: {}", e);
        }
        filterChain.doFilter(request,response);
    }
}
