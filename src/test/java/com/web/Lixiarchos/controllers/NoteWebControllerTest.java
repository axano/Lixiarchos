package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.model.Note;
import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.repositories.NoteRepository;
import com.web.Lixiarchos.repositories.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoteWebControllerTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private Model model;

    @InjectMocks
    private NoteWebController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // -------------------------------------------------------------
    // GET /notes/new/{personId}
    // -------------------------------------------------------------
    @Test
    void showNoteForm_validPersonId_returnsForm() {
        Person person = new Person();
        person.setId(1);
        person.setName("John");
        person.setSurname("Doe");

        when(personRepository.findById(1)).thenReturn(Optional.of(person));

        String view = controller.showNoteForm(1, model);

        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(model).addAttribute(eq("note"), noteCaptor.capture());

        Note passedNote = noteCaptor.getValue();
        assertEquals(person, passedNote.getPerson());
        assertNotNull(passedNote.getDateRegistered());

        verify(model).addAttribute("personName", "John Doe");
        verify(model).addAttribute("personId", 1);

        assertEquals("note-form", view);
    }

    @Test
    void showNoteForm_invalidPersonId_throwsException() {
        when(personRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> controller.showNoteForm(1, model));
    }

    // -------------------------------------------------------------
    // POST /notes/save
    // -------------------------------------------------------------
    @Test
    void saveNote_savesAndRedirects() {
        Person person = new Person();
        person.setId(10);

        Note note = new Note();
        note.setPerson(person);

        String redirect = controller.saveNote(note);

        verify(noteRepository).save(note);
        assertEquals("redirect:/persons/details/10", redirect);
    }

    // -------------------------------------------------------------
    // POST /notes (createNote)
    // -------------------------------------------------------------
    @Test
    void createNote_attachesPersonAndSaves() {
        Person p = new Person();
        p.setId(5);

        Note note = new Note();

        when(personRepository.findById(5)).thenReturn(Optional.of(p));

        String redirect = controller.createNote(note, 5);

        assertEquals(p, note.getPerson());
        verify(noteRepository).save(note);
        assertEquals("redirect:/persons/details/5", redirect);
    }

    @Test
    void createNote_invalidPersonId_throwsException() {
        Note note = new Note();
        when(personRepository.findById(5)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> controller.createNote(note, 5));
    }

    // -------------------------------------------------------------
    // GET /notes/edit/{noteId}
    // -------------------------------------------------------------
    @Test
    void showEditNoteForm_validNoteId() {
        Person p = new Person();
        p.setId(10);

        Note note = new Note();
        note.setId(1);
        note.setPerson(p);

        when(noteRepository.findById(1)).thenReturn(Optional.of(note));

        String view = controller.showEditNoteForm(1, model);

        verify(model).addAttribute("note", note);
        verify(model).addAttribute("personId", 10);
        assertEquals("note-form", view);
    }

    @Test
    void showEditNoteForm_invalidNoteId_throwsException() {
        when(noteRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> controller.showEditNoteForm(1, model));
    }

    // -------------------------------------------------------------
    // POST /notes/update/{noteId}
    // -------------------------------------------------------------
    @Test
    void updateNote_updatesExistingAndRedirects() {
        Person p = new Person();
        p.setId(99);

        Note existing = new Note();
        existing.setId(1);
        existing.setPerson(p);

        Note updated = new Note();
        updated.setContent("new content");
        updated.setDateRegistered((LocalDate.of(1000, 1, 1)));

        when(noteRepository.findById(1)).thenReturn(Optional.of(existing));
        when(personRepository.findById(99)).thenReturn(Optional.of(p));

        String redirect = controller.updateNote(1, updated, 99);

        assertEquals("new content", existing.getContent());
        assertEquals(updated.getDateRegistered(), existing.getDateRegistered());
        assertEquals(p, existing.getPerson());

        verify(noteRepository).save(existing);
        assertEquals("redirect:/persons/details/99", redirect);
    }

    @Test
    void updateNote_invalidNoteId_throwsException() {
        when(noteRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> controller.updateNote(1, new Note(), 5));
    }

    @Test
    void updateNote_invalidPersonId_throwsException() {
        Note existing = new Note();
        existing.setId(1);
        when(noteRepository.findById(1)).thenReturn(Optional.of(existing));
        when(personRepository.findById(5)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> controller.updateNote(1, new Note(), 5));
    }

    // -------------------------------------------------------------
    // GET /notes/delete/{noteId}
    // -------------------------------------------------------------
    @Test
    void deleteNote_validNoteId_deletesAndRedirects() {
        Person p = new Person();
        p.setId(7);

        Note n = new Note();
        n.setId(1);
        n.setPerson(p);

        when(noteRepository.findById(1)).thenReturn(Optional.of(n));

        String redirect = controller.deleteNote(1);

        verify(noteRepository).delete(n);
        assertEquals("redirect:/persons/details/7", redirect);
    }

    @Test
    void deleteNote_invalidNoteId_throwsException() {
        when(noteRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> controller.deleteNote(1));
    }
}
