package com.web.Lixiarchos.model;

import com.web.Lixiarchos.enums.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

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

        // Mix valid and invalid
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

    // ---------------- Helper ----------------
    private Person createValidPerson() {
        Person person = new Person();
        person.setName("John");
        person.setSurname("Doe");
        person.setEmail("test@example.com");
        person.setTelephone("+1234567890");
        person.setLanguagesString("ENGLISH"); // initial valid language
        person.setDateOfBirth(new java.util.Date());
        return person;
    }
}
