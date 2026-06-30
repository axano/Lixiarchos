package com.web.Lixiarchos.model;

import com.web.Lixiarchos.model.exceptions.InteractionNotFoundException;
import com.web.Lixiarchos.model.exceptions.NoteNotFoundException;
import com.web.Lixiarchos.model.exceptions.PersonNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    @Test
    void personNotFoundException_isRuntimeException() {
        assertInstanceOf(RuntimeException.class, new PersonNotFoundException());
    }

    @Test
    void personNotFoundException_hasCorrectMessage() {
        assertEquals("Person not found", new PersonNotFoundException().getMessage());
    }

    @Test
    void personNotFoundException_annotatedWithNotFound() {
        ResponseStatus annotation = PersonNotFoundException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.NOT_FOUND, annotation.value());
    }

    @Test
    void interactionNotFoundException_isRuntimeException() {
        assertInstanceOf(RuntimeException.class, new InteractionNotFoundException());
    }

    @Test
    void interactionNotFoundException_hasCorrectMessage() {
        assertEquals("Interaction not found", new InteractionNotFoundException().getMessage());
    }

    @Test
    void interactionNotFoundException_annotatedWithNotFound() {
        ResponseStatus annotation = InteractionNotFoundException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.NOT_FOUND, annotation.value());
    }

    @Test
    void noteNotFoundException_isRuntimeException() {
        assertInstanceOf(RuntimeException.class, new NoteNotFoundException());
    }

    @Test
    void noteNotFoundException_hasCorrectMessage() {
        assertEquals("Note not found", new NoteNotFoundException().getMessage());
    }

    @Test
    void noteNotFoundException_annotatedWithNotFound() {
        ResponseStatus annotation = NoteNotFoundException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.NOT_FOUND, annotation.value());
    }
}
