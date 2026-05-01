package app.kehdo.backend.auth.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DisposableEmailValidatorTest {

    private DisposableEmailValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DisposableEmailValidator();
        validator.load();
    }

    @Test
    void loads_a_meaningful_number_of_domains() {
        // Sanity check — if the resource file shrinks unexpectedly we want to know.
        assertThat(validator.size()).isGreaterThan(50);
    }

    @Test
    void blocks_known_disposable_services() {
        assertThat(validator.findBlockedDomain("user@mailinator.com")).isEqualTo("mailinator.com");
        assertThat(validator.findBlockedDomain("user@guerrillamail.com")).isEqualTo("guerrillamail.com");
        assertThat(validator.findBlockedDomain("user@10minutemail.com")).isEqualTo("10minutemail.com");
        assertThat(validator.findBlockedDomain("user@yopmail.com")).isEqualTo("yopmail.com");
    }

    @Test
    void allows_legitimate_provider_domains() {
        assertThat(validator.findBlockedDomain("user@gmail.com")).isNull();
        assertThat(validator.findBlockedDomain("user@yahoo.com")).isNull();
        assertThat(validator.findBlockedDomain("user@outlook.com")).isNull();
        assertThat(validator.findBlockedDomain("user@icloud.com")).isNull();
        assertThat(validator.findBlockedDomain("user@protonmail.com")).isNull();
        assertThat(validator.findBlockedDomain("user@kehdo.app")).isNull();
    }

    @Test
    void domain_check_is_case_insensitive() {
        assertThat(validator.findBlockedDomain("user@MAILINATOR.COM")).isEqualTo("mailinator.com");
        assertThat(validator.findBlockedDomain("user@MailInator.Com")).isEqualTo("mailinator.com");
    }

    @Test
    void only_the_domain_part_matters_local_part_is_ignored() {
        assertThat(validator.findBlockedDomain("aaa@mailinator.com")).isEqualTo("mailinator.com");
        assertThat(validator.findBlockedDomain("zzz@mailinator.com")).isEqualTo("mailinator.com");
    }

    @Test
    void malformed_email_without_at_returns_null_safely() {
        assertThat(validator.findBlockedDomain("not-an-email")).isNull();
        assertThat(validator.findBlockedDomain("trailing-at@")).isNull();
        assertThat(validator.findBlockedDomain("")).isNull();
    }

    @Test
    void null_email_returns_null_safely() {
        assertThat(validator.findBlockedDomain(null)).isNull();
    }
}
