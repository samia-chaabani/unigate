package com.unigate.registration;

import com.unigate.common.security.JwtTokenProvider;
import com.unigate.exception.BusinessException;
import com.unigate.registration.dto.RegisterRequest;
import com.unigate.registration.entity.RefreshToken;
import com.unigate.registration.enums.RegistrationType;
import com.unigate.registration.repository.RefreshTokenRepository;
import com.unigate.registration.repository.UserRepository;
import com.unigate.registration.service.AuthService;
import com.unigate.registration.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de AuthService.register (base de données simulée avec Mockito).
 * - CT-EF01-002 : email déjà utilisé -> « Email already registered ».
 * - CT-EF01-013 : même email en MAJUSCULES -> doit aussi être refusé.
 *   Ce second test ÉCHOUE aujourd'hui : il met en évidence la cause du bug
 *   (AuthService ne normalise pas l'email avant existsByEmail).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceEmailTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock AuthenticationManager authenticationManager;
    @Mock RefreshTokenService refreshTokenService;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @InjectMocks AuthService authService;

    @BeforeEach
    void stubs() {
        // La base simulée : « abc@test.com » existe déjà (en minuscules, tel qu'enregistré).
        when(userRepository.existsByEmail("abc@test.com")).thenReturn(true);
        // Le reste de register() ne doit pas échouer si l'email est accepté.
        RefreshToken rt = mock(RefreshToken.class);
        when(rt.getToken()).thenReturn("refresh-token");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(rt);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(jwtTokenProvider.generateToken(any())).thenReturn("jwt");
    }

    private RegisterRequest request(String email) {
        RegisterRequest r = new RegisterRequest();
        r.setFirstName("Test");
        r.setLastName("Qa");
        r.setEmail(email);
        r.setPassword("Test1234!");
        r.setRegistrationType(RegistrationType.FIRST_YEAR_ING);
        return r;
    }

    @Test
    void CT_EF01_002_emailDejaUtilise_refuse() {
        assertThatThrownBy(() -> authService.register(request("abc@test.com")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email already registered");
    }

    @Test
    void CT_EF01_013_emailDejaUtilise_enMajuscules_doitEtreRefuse() {
        assertThatThrownBy(() -> authService.register(request("ABC@TEST.COM")))
                .as("« ABC@TEST.COM » est le même email que « abc@test.com » : doublon à refuser")
                .isInstanceOf(BusinessException.class)
                .hasMessage("Email already registered");
    }
}