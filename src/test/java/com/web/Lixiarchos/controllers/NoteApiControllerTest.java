package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.model.Note;
import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.model.exceptions.NoteNotFoundException;
import com.web.Lixiarchos.repositories.NoteRepository;
import com.web.Lixiarchos.repositories.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class NoteApiControllerTest {

    private NoteRepository noteRepository;
    private PersonRepository personRepository;
    private NoteApiController controller;

    private Person person;
    private Note note;

    @BeforeEach
    void setUp() {
        noteRepository = mock(NoteRepository.class);
        personRepository = mock(PersonRepository.class);
        controller = new NoteApiController(noteRepository, personRepository);

        person = new Person();
        person.setId(1);

        note = new Note();
        note.setId(1);
        note.setPerson(person);
        note.setContent("Test note");
    }

    // --- GET all notes ---
    @Test
    void getAllNotes_ReturnsAllNotes() {
        List<Note> notes = Arrays.asList(note);
        when(noteRepository.findAll()).thenReturn(notes);

        List<Note> result = controller.getAllNotes();

        assertThat(result).isEqualTo(notes);
        verify(noteRepository).findAll();
    }

    // --- GET note by ID ---
    @Test
    void getNoteById_Found_ReturnsNote() {
        when(noteRepository.findById(1)).thenReturn(Optional.of(note));

        Note result = controller.getNoteById(1);

        assertThat(result).isEqualTo(note);
    }

    @Test
    void getNoteById_NotFound_ThrowsNoteNotFoundException() {
        when(noteRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> controller.getNoteById(1));
    }

    // --- POST create note ---
    @Test
    void createNote_ValidNote_SavesNote() {
        when(personRepository.existsById(1)).thenReturn(true);
        when(noteRepository.save(note)).thenReturn(note);

        Note result = controller.createNote(note);

        assertThat(result).isEqualTo(note);
        verify(noteRepository).save(note);
    }

    @Test
    void createNote_NoPerson_ThrowsException() {
        note.setPerson(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createNote(note));

        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).isEqualTo("Person ID is required");
    }

    @Test
    void createNote_PersonDoesNotExist_ThrowsException() {
        when(personRepository.existsById(1)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createNote(note));

        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).isEqualTo("Person with ID 1 does not exist");
    }

    // --- PUT update note ---
    @Test
    void updateNote_ExistingNote_UpdatesSuccessfully() {
        when(noteRepository.existsById(1)).thenReturn(true);

        controller.updateNote(note, 1);

        verify(noteRepository).save(note);
        assertThat(note.getId()).isEqualTo(1);
    }

    @Test
    void updateNote_NonExistingNote_ThrowsNoteNotFoundException() {
        when(noteRepository.existsById(1)).thenReturn(false);

        assertThrows(NoteNotFoundException.class, () -> controller.updateNote(note, 1));
    }

    // --- DELETE note ---
    @Test
    void deleteNote_ExistingNote_DeletesSuccessfully() {
        when(noteRepository.existsById(1)).thenReturn(true);

        controller.deleteNote(1);

        verify(noteRepository).deleteById(1);
    }

    @Test
    void deleteNote_NonExistingNote_ThrowsNoteNotFoundException() {
        when(noteRepository.existsById(1)).thenReturn(false);

        assertThrows(NoteNotFoundException.class, () -> controller.deleteNote(1));
    }

    // --- GET notes by person ID ---
    @Test
    void getNotesByPersonId_ValidPerson_ReturnsNotes() {
        List<Note> notes = Arrays.asList(note);
        when(personRepository.existsById(1)).thenReturn(true);
        when(noteRepository.findByPersonId(1)).thenReturn(notes);

        List<Note> result = controller.getNotesByPersonId(1);

        assertThat(result).isEqualTo(notes);
        verify(noteRepository).findByPersonId(1);
    }

    @Test
    void getNotesByPersonId_InvalidPerson_ThrowsException() {
        when(personRepository.existsById(99)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.getNotesByPersonId(99));

        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).isEqualTo("Person with ID 99 does not exist");
    }
}
