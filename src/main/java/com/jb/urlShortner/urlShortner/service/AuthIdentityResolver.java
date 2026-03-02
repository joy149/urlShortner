package com.jb.urlShortner.urlShortner.service;

import com.jb.urlShortner.urlShortner.domain.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class AuthIdentityResolver {

    private final AuthTokenService authTokenService;

    public AuthIdentityResolver(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    public Optional<AuthenticatedUser> resolve(Authentication authentication, HttpServletRequest request) {
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String login = oauth2User.getAttribute("login");
            String resolvedLogin = Objects.nonNull(login) && !login.isBlank() ? login : authentication.getName();
            return Optional.of(new AuthenticatedUser(
                    resolvedLogin,
                    oauth2User.getAttribute("email"),
                    oauth2User.getAttribute("name")
            ));
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        return authTokenService.parseToken(token);
    }
}
