package com.unigate.registration;

import com.unigate.registration.dto.RegisterRequest;
import com.unigate.registration.enums.RegistrationType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CT-EF01-008 : validation de l'email et du mot de passe (niveau unitaire).
 * On teste les règles de RegisterRequest (@NotBlank, @Email, @Size(min = 8))
 * directement avec le validateur Jakarta, sans démarrer Spring ni la base.
 */
class RegisterRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    /** Requête d'inscription valide : chaque test modifie UN seul champ. */
    private RegisterRequest valid() {
        RegisterRequest r = new RegisterRequest();
        r.setFirstName("Test");
        r.setLastName("Qa");
        r.setEmail("test.qa@enicar.ucar.tn");
        r.setPassword("Test1234!");
        r.setRegistrationType(RegistrationType.FIRST_YEAR_ING);
        return r;
    }

    private boolean hasViolationOn(RegisterRequest r, String field) {
        return validator.validate(r).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    @Test
    void requeteValide_aucuneErreur() {
        assertThat(validator.validate(valid())).isEmpty();
    }

    // ----- Email -----
    @ParameterizedTest(name = "email invalide refusé : \"{0}\"")
    @ValueSource(strings = {"abc", "abc.com", "abc@", "@test.com", "a b@test.com"})
    void emailInvalide_refuse(String email) {
        RegisterRequest r = valid();
        r.setEmail(email);
        assertThat(hasViolationOn(r, "email")).isTrue();
    }

    @ParameterizedTest(name = "email valide accepté : \"{0}\"")
    @ValueSource(strings = {"abc@test.com", "prenom.nom@enicar.ucar.tn", "ABC@TEST.COM"})
    void emailValide_accepte(String email) {
        RegisterRequest r = valid();
        r.setEmail(email);
        assertThat(hasViolationOn(r, "email")).isFalse();
    }

    @Test
    void emailSansPoint_accepte_commeObserveEnManuel() {
        // Observé dans CT-EF01-005 : « abc@test » (sans .com) est accepté par @Email.
        RegisterRequest r = valid();
        r.setEmail("abc@test");
        assertThat(hasViolationOn(r, "email")).isFalse();
    }

    @Test
    void emailVide_refuse() {
        RegisterRequest r = valid();
        r.setEmail("");
        assertThat(hasViolationOn(r, "email")).isTrue();
    }

    // ----- Mot de passe -----
    @ParameterizedTest(name = "mot de passe \"{0}\" -> valide = {1}")
    @CsvSource({
            "1234567,false",     // 7 caractères : refusé
            "12345678,true",     // 8 caractères : accepté
            "abcdefgh,true",     // aucune règle de complexité
            "abcd1234,true",
            "Test1234!,true"
    })
    void motDePasse_longueurMinimale8(String password, boolean attendu) {
        RegisterRequest r = valid();
        r.setPassword(password);
        assertThat(!hasViolationOn(r, "password")).isEqualTo(attendu);
    }

    @Test
    void motDePasseVide_refuse() {
        RegisterRequest r = valid();
        r.setPassword("");
        assertThat(hasViolationOn(r, "password")).isTrue();
    }

    // ----- Noms (CT-EF01-015 / CT-EF01-006 au niveau unitaire) -----
    @Test
    void prenomEtNomComposesDespaces_refuses_parLeServeur() {
        RegisterRequest r = valid();
        r.setFirstName("   ");
        r.setLastName("   ");
        assertThat(hasViolationOn(r, "firstName")).isTrue();
        assertThat(hasViolationOn(r, "lastName")).isTrue();
    }

    @Test
    void prenomDe256Caracteres_accepte_aucuneLimiteDeLongueur() {
        RegisterRequest r = valid();
        r.setFirstName("A".repeat(256));
        assertThat(hasViolationOn(r, "firstName")).isFalse();
    }
}