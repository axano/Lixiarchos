package com.web.Lixiarchos.model;

import com.web.Lixiarchos.enums.Language;
import com.web.Lixiarchos.enums.Sex;
import com.web.Lixiarchos.enums.Religion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PersonValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ---------------- Languages dynamic validation ----------------
    @Test
    void testLanguagesValidDynamic() {
        Person person = createValidPerson();

        // Populate all enum values dynamically
        String allLanguages = Stream.of(Language.values())
                .map(Enum::name)
                .collect(Collectors.joining(","));
        person.setLanguagesString(allLanguages);

        Set<ConstraintViolation<Person>> violations = validator.validate(person);

        // No validation errors on languages
        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("languages"));

        // Check that all enum values are present in the Person.languages set
        for (Language lang : Language.values()) {
            assertThat(person.getLanguages()).contains(lang);
        }
    }

    @Test
    void testLanguagesInvalidIgnored() {
        Person person = createValidPerson();

        // Mix valid and invalid values
        String input = "ENGLISH,INVALID,DUTCH,UNKNOWN";
        person.setLanguagesString(input);

        Set<ConstraintViolation<Person>> violations = validator.validate(person);

        // No constraint violations because invalid entries are ignored
        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("languages"));

        // Only valid enums remain
        assertThat(person.getLanguages()).contains(Language.ENGLISH, Language.DUTCH);
        // Invalid values should not appear
        assertThat(person.getLanguages().size()).isEqualTo(2);
    }

    // ---------------- Field constraints ----------------

    @Test
    void nameNull_producesViolation() {
        Person person = createValidPerson();
        person.setName(null);
        assertThat(validator.validate(person))
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void nameEmpty_producesViolation() {
        Person person = createValidPerson();
        person.setName("");
        assertThat(validator.validate(person))
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void nameTooLong_producesViolation() {
        Person person = createValidPerson();
        person.setName("A".repeat(33));
        assertThat(validator.validate(person))
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    void surnameNull_producesViolation() {
        Person person = createValidPerson();
        person.setSurname(null);
        assertThat(validator.validate(person))
                .anyMatch(v -> v.getPropertyPath().toString().equals("surname"));
    }

    @Test
    void surnameEmpty_producesViolation() {
        Person person = createValidPerson();
        person.setSurname("");
        assertThat(validator.validate(person))
                .anyMatch(v -> v.getPropertyPath().toString().equals("surname"));
    }

    @Test
    void dateOfBirthNull_producesViolation() {
        Person person = createValidPerson();
        person.setDateOfBirth(null);
        assertThat(validator.validate(person))
                .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth"));
    }

    @Test
    void invalidEmail_producesViolation() {
        Person person = createValidPerson();
        person.setEmail("not-an-email");
        assertThat(validator.validate(person))
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void nullEmail_noViolation() {
        // @Email does not apply to null — null is allowed
        Person person = createValidPerson();
        person.setEmail(null);
        assertThat(validator.validate(person))
                .noneMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void invalidTelephone_producesViolation() {
        Person person = createValidPerson();
        person.setTelephone("abc");
        assertThat(validator.validate(person))
                .anyMatch(v -> v.getPropertyPath().toString().equals("telephone"));
    }

    @Test
    void emptyTelephone_noViolation() {
        // regex allows empty string via ^$ alternative
        Person person = createValidPerson();
        person.setTelephone("");
        assertThat(validator.validate(person))
                .noneMatch(v -> v.getPropertyPath().toString().equals("telephone"));
    }

    @Test
    void nullTelephone_noViolation() {
        // @Pattern does not apply to null
        Person person = createValidPerson();
        person.setTelephone(null);
        assertThat(validator.validate(person))
                .noneMatch(v -> v.getPropertyPath().toString().equals("telephone"));
    }

    @Test
    void validPerson_zeroViolations() {
        assertThat(validator.validate(createValidPerson())).isEmpty();
    }

    // ---------------- setUsernamesString / getUsernamesString ----------------

    @Test
    void setUsernamesString_commaSeparated_parsesAll() {
        Person person = createValidPerson();
        person.setUsernamesString("user1,user2,user3");
        assertThat(person.getUsernames()).containsExactlyInAnyOrder("user1", "user2", "user3");
    }

    @Test
    void setUsernamesString_nullInput_givesEmptySet() {
        Person person = createValidPerson();
        person.setUsernamesString(null);
        assertThat(person.getUsernames()).isEmpty();
    }

    @Test
    void setUsernamesString_emptyInput_givesEmptySet() {
        Person person = createValidPerson();
        person.setUsernamesString("");
        assertThat(person.getUsernames()).isEmpty();
    }

    @Test
    void setUsernamesString_blankEntries_skipped() {
        Person person = createValidPerson();
        person.setUsernamesString("user1,,user2");
        assertThat(person.getUsernames()).containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void getUsernamesString_multipleUsernames_joinedByComma() {
        Person person = createValidPerson();
        person.setUsernames(new java.util.HashSet<>(java.util.Set.of("alpha", "beta")));
        String result = person.getUsernamesString();
        assertThat(result).contains("alpha");
        assertThat(result).contains("beta");
    }

    // ---------------- Helper ----------------
    private Person createValidPerson() {
        Person person = new Person();
        person.setName("John");
        person.setSurname("Doe");
        person.setEmail("test@example.com");
        person.setTelephone("+1234567890");
        person.setLanguagesString("ENGLISH"); // initial valid language
        person.setDateOfBirth(LocalDate.of(1990, 1, 1)); // ✅ LocalDate now
        person.setSex(Sex.MALE);
        person.setReligion(Religion.CHRISTIAN_ORTHODOX);
        person.setIsFelon(false);
        person.setAddress("123 Test St");
        return person;
    }
}
