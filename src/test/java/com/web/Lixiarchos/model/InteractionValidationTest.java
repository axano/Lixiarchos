package com.web.Lixiarchos.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InteractionValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private Interaction validInteraction() {
        Interaction i = new Interaction();
        i.setContent("They had a meeting.");
        return i;
    }

    @Test
    void validInteraction_noViolations() {
        Set<ConstraintViolation<Interaction>> violations = validator.validate(validInteraction());
        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("content"));
    }

    @Test
    void nullContent_producesViolation() {
        Interaction i = validInteraction();
        i.setContent(null);

        Set<ConstraintViolation<Interaction>> violations = validator.validate(i);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("content"));
    }

    @Test
    void emptyContent_producesViolation() {
        Interaction i = validInteraction();
        i.setContent("");

        Set<ConstraintViolation<Interaction>> violations = validator.validate(i);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("content"));
    }

    @Test
    void nullInteractionType_noViolation() {
        // interactionType has no @NotNull constraint — null is allowed
        Interaction i = validInteraction();
        i.setInteractionType(null);

        Set<ConstraintViolation<Interaction>> violations = validator.validate(i);

        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("interactionType"));
    }
}
