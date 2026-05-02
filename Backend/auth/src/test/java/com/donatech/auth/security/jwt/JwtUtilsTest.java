package com.donatech.auth.security.jwt;

import com.donatech.auth.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    // 64-byte Base64 key valid for HS256
    private static final String TEST_SECRET =
            "dG9rZW5TZWNyZXRLZXlGb3JEb25hdGVjaFRlc3RpbmdQdXJwb3Nlc09ubHkxMjM0NTY3ODk=";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000);
    }

    private UserDetailsImpl buildPrincipal() {
        return new UserDetailsImpl(1L, "user@donatech.cl", "pass", "ROLE_DONANTE");
    }

    @Test
    void generateToken_validAuthentication_returnsNonNullToken() {
        UserDetailsImpl principal = buildPrincipal();
        when(authentication.getPrincipal()).thenReturn(principal);

        String token = jwtUtils.generateJwtToken(authentication);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void extractUsername_validToken_returnsCorrectEmail() {
        UserDetailsImpl principal = buildPrincipal();
        when(authentication.getPrincipal()).thenReturn(principal);
        String token = jwtUtils.generateJwtToken(authentication);

        String username = jwtUtils.getUserNameFromJwtToken(token);

        assertThat(username).isEqualTo("user@donatech.cl");
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        UserDetailsImpl principal = buildPrincipal();
        when(authentication.getPrincipal()).thenReturn(principal);
        String token = jwtUtils.generateJwtToken(authentication);

        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
    }

    @Test
    void validateToken_malformedToken_returnsFalse() {
        assertThat(jwtUtils.validateJwtToken("not.a.valid.token")).isFalse();
    }

    @Test
    void validateToken_emptyString_returnsFalse() {
        assertThat(jwtUtils.validateJwtToken("")).isFalse();
    }
}
