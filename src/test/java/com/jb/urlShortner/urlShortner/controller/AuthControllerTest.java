package com.jb.urlShortner.urlShortner.controller;

import com.jb.urlShortner.urlShortner.domain.AuthenticatedUser;
import com.jb.urlShortner.urlShortner.service.AuthIdentityResolver;
import com.jb.urlShortner.urlShortner.service.AuthTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthTokenService authTokenService;
    @Mock
    private AuthIdentityResolver authIdentityResolver;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    @Mock
    private Authentication authentication;
    @Mock
    private OAuth2User oauth2User;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController("https://app.test/?postLogin=1", authTokenService, authIdentityResolver);
    }

    @Test
    void meReturnsUnauthorizedWhenNoIdentity() {
        when(authIdentityResolver.resolve(authentication, request)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.me(authentication, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void meReturnsAuthenticatedPayloadForTokenIdentity() {
        when(authIdentityResolver.resolve(authentication, request))
                .thenReturn(Optional.of(new AuthenticatedUser("alice", "alice@example.com", "Alice")));
        when(authentication.getPrincipal()).thenReturn("not-oauth");

        ResponseEntity<?> response = controller.me(authentication, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue((Boolean) body.get("authenticated"));
        assertEquals("alice", body.get("login"));
    }

    @Test
    void meAddsOAuthAttributesWhenSessionAuthenticationPresent() {
        when(authIdentityResolver.resolve(authentication, request))
                .thenReturn(Optional.of(new AuthenticatedUser("alice", "alice@example.com", "Alice")));
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("avatar_url")).thenReturn("https://avatar");
        when(oauth2User.getAttributes()).thenReturn(Map.of("k", "v"));

        ResponseEntity<?> response = controller.me(authentication, request);

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("https://avatar", body.get("avatarUrl"));
        assertEquals(Map.of("k", "v"), body.get("attributes"));
    }

    @Test
    void loginStoresRedirectAndUsesGithubByDefault() {
        when(request.getSession(true)).thenReturn(session);

        ResponseEntity<?> response = controller.login(null, "https://frontend.test/?postLogin=1", null, request);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals("/oauth2/authorization/github", response.getHeaders().getFirst("Location"));
        verify(session).setAttribute("oauth_redirect_uri", "https://frontend.test/?postLogin=1");
    }

    @Test
    void loginUsesGoogleWhenProviderIsGoogle() {
        when(request.getSession(true)).thenReturn(session);

        ResponseEntity<?> response = controller.login("google", null, "https://front.test/?postLogin=1", request);

        assertEquals("/oauth2/authorization/google", response.getHeaders().getFirst("Location"));
    }

    @Test
    void successRedirectsWithIssuedToken() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("oauth_redirect_uri")).thenReturn("https://frontend.test/?postLogin=1");
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("login")).thenReturn("alice");
        when(oauth2User.getAttribute("email")).thenReturn("alice@example.com");
        when(oauth2User.getAttribute("name")).thenReturn("Alice");
        when(authTokenService.issueToken(new AuthenticatedUser("alice", "alice@example.com", "Alice")))
                .thenReturn("signed.token");

        ResponseEntity<?> response = controller.success(request, authentication);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals("https://frontend.test/?postLogin=1&token=signed.token", response.getHeaders().getFirst("Location"));
        verify(session).removeAttribute("oauth_redirect_uri");
    }

    @Test
    void failureRedirectsWithAuthError() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("oauth_redirect_uri")).thenReturn("https://frontend.test/?postLogin=1");

        ResponseEntity<?> response = controller.failure("denied", request);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals("https://frontend.test/?postLogin=1&authError=denied", response.getHeaders().getFirst("Location"));
        verify(session).removeAttribute("oauth_redirect_uri");
    }
}
