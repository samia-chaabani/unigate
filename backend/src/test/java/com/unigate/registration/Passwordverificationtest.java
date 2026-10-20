package com.unigate.registration;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CT-EF02-009 : vérification du mot de passe (niveau unitaire).
 * UniGate utilise BCryptPasswordEncoder (voir SecurityConfig.passwordEncoder()).
 */
class PasswordVerificationTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void bonMotDePasse_accepte() {
        String hash = encoder.encode("Test1234!");
        assertThat(encoder.matches("Test1234!", hash)).isTrue();
    }

    @Test
    void mauvaisMotDePasse_refuse() {
        String hash = encoder.encode("Test1234!");
        assertThat(encoder.matches("Mauvais123", hash)).isFalse();
    }

    @Test
    void motDePasseDifferantSeulementParLaCasse_refuse() {
        String hash = encoder.encode("Test1234!");
        assertThat(encoder.matches("test1234!", hash)).isFalse();
    }

    @Test
    void motDePasseVide_refuse() {
        String hash = encoder.encode("Test1234!");
        assertThat(encoder.matches("", hash)).isFalse();
    }

    @Test
    void motDePasseNEstJamaisStockeEnClair() {
        String hash = encoder.encode("Test1234!");
        assertThat(hash).isNotEqualTo("Test1234!");
        assertThat(hash).startsWith("$2");   // format BCrypt
    }

    @Test
    void deuxHashDuMemeMotDePasse_sontDifferents_maisValides() {
        String h1 = encoder.encode("Test1234!");
        String h2 = encoder.encode("Test1234!");
        assertThat(h1).isNotEqualTo(h2);               // sel aléatoire
        assertThat(encoder.matches("Test1234!", h1)).isTrue();
        assertThat(encoder.matches("Test1234!", h2)).isTrue();
    }
}