package com.web.Lixiarchos.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NoteValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Note validNote() {
        Note note = new Note();
        note.setContent("Some note content.");
        return note;
    }

    @Test
    void validNote_noViolations() {
        Set<ConstraintViolation<Note>> violations = validator.validate(validNote());
        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("content"));
    }

    @Test
    void nullContent_producesViolation() {
        Note note = validNote();
        note.setContent(null);

        Set<ConstraintViolation<Note>> violations = validator.validate(note);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("content"));
    }

    @Test
    void emptyContent_producesViolation() {
        Note note = validNote();
        note.setContent("");

        Set<ConstraintViolation<Note>> violations = validator.validate(note);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("content"));
    }

    @Test
    void nullDateRegistered_noViolation() {
        // dateRegistered has no @NotNull — null is allowed
        Note note = validNote();
        note.setDateRegistered(null);

        Set<ConstraintViolation<Note>> violations = validator.validate(note);

        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("dateRegistered"));
    }
}
