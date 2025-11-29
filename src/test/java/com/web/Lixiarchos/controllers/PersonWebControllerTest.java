package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.enums.Language;
import com.web.Lixiarchos.enums.Religion;
import com.web.Lixiarchos.enums.Sex;
import com.web.Lixiarchos.model.Interaction;
import com.web.Lixiarchos.model.Note;
import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.model.exceptions.PersonNotFoundException;
import com.web.Lixiarchos.repositories.InteractionRepository;
import com.web.Lixiarchos.repositories.NoteRepository;
import com.web.Lixiarchos.repositories.PersonRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonWebControllerTest {

    @Mock private PersonRepository personRepository;
    @Mock private NoteRepository noteRepository;
    @Mock private InteractionRepository interactionRepository;
    @Mock private Model model;
    @Mock private HttpServletRequest request;

    @InjectMocks
    private PersonWebController controller;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        when(request.getAttribute("cspNonce")).thenReturn("test-nonce");
    }

    // --------------------------------------------------------------------
    @Test
    void showAllPersons_returnsViewAndModel() {
        List<Person> persons = List.of(new Person(), new Person());
        when(personRepository.findAll()).thenReturn(persons);

        String result = controller.showAllPersons(model, request);

        verify(model).addAttribute("persons", persons);
        verify(model).addAttribute("cspNonce", "test-nonce");
        assertEquals("persons", result);
    }

    // --------------------------------------------------------------------
    @Test
    void showEditForm_validId_returnsForm() {
        Person p = new Person();
        p.setId(1);
        when(personRepository.findById(1)).thenReturn(Optional.of(p));

        String view = controller.showEditForm(1, model, request);

        verify(model).addAttribute("person", p);
        verify(model).addAttribute("sexOptions", Sex.values());
        verify(model).addAttribute("religionOptions", Religion.values());
        verify(model).addAttribute("languageOptions", Language.values());
        verify(model).addAttribute("cspNonce", "test-nonce");

        assertEquals("person-form", view);
    }

    @Test
    void showEditForm_invalidId_throwsException() {
        when(personRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(PersonNotFoundException.class, () ->
                controller.showEditForm(1, model, request)
        );
    }

    // --------------------------------------------------------------------
    @Test
    void createPerson_setsDefaultFelonWhenNull() {
        Person p = new Person();
        p.setIsFelon(null);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        controller.createPerson(p, bindingResult, model, request);

        assertFalse(p.getIsFelon());
        verify(personRepository).save(p);
    }

    @Test
    void createPerson_redirectsAfterSaving() {
        Person p = new Person();
        p.setIsFelon(false);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = controller.createPerson(p, bindingResult, model, request);

        verify(personRepository).save(p);
        assertEquals("redirect:/persons", result);
    }

    @Test
    void createPerson_validationError_returnsForm() {
        Person p = new Person();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = controller.createPerson(p, bindingResult, model, request);

        verify(model).addAttribute("errorMessage", "So you think you're smart? Don't mess with the values.");
        verify(model).addAttribute("person", p);
        verify(model).addAttribute("cspNonce", "test-nonce");
        assertEquals("person-form", view);
        verify(personRepository, never()).save(any());
    }

    // --------------------------------------------------------------------
    @Test
    void showCreateForm_setsDefaults() {
        String view = controller.showCreateForm(model, request);

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(model).addAttribute(eq("person"), captor.capture());
        Person created = captor.getValue();

        assertEquals(LocalDate.of(1000, 1, 1), created.getDateOfBirth());

        verify(model).addAttribute("sexOptions", Sex.values());
        verify(model).addAttribute("religionOptions", Religion.values());
        verify(model).addAttribute("languageOptions", Language.values());
        verify(model).addAttribute("cspNonce", "test-nonce");

        assertEquals("person-form", view);
    }

    // --------------------------------------------------------------------
    @Test
    void updatePerson_setsIdAndSaves() {
        Person p = new Person();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        String result = controller.updatePerson(5, p, bindingResult, model, request);

        assertEquals(5, p.getId());
        verify(personRepository).save(p);
        assertEquals("redirect:/persons", result);
    }

    @Test
    void updatePerson_validationError_returnsForm() {
        Person p = new Person();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = controller.updatePerson(5, p, bindingResult, model, request);

        verify(model).addAttribute("errorMessage", "So you think you're smart? Don't mess with the values.");
        verify(model).addAttribute("person", p);
        verify(model).addAttribute("cspNonce", "test-nonce");
        assertEquals("person-form", view);
        verify(personRepository, never()).save(any());
    }

    // --------------------------------------------------------------------
    @Test
    void deletePerson_deletesSuccessfully() {
        String view = controller.deletePerson(10, model, request);

        verify(personRepository).deleteById(10);
        assertEquals("redirect:/persons", view);
    }

    // --------------------------------------------------------------------
    @Test
    void showPersonDetails_loadsNotesAndInteractions() {
        Person main = new Person();
        main.setId(1);
        main.setName("John");
        main.setSurname("Smith");

        Person other = new Person();
        other.setId(2);
        other.setName("Jane");
        other.setSurname("Doe");

        Interaction inter = new Interaction();
        inter.setPersonA(main);
        inter.setPersonB(other);

        when(personRepository.findById(1)).thenReturn(Optional.of(main));
        when(noteRepository.findByPersonId(1)).thenReturn(List.of(new Note()));
        when(interactionRepository.findByPersonAOrPersonB(main, main)).thenReturn(List.of(inter));

        String view = controller.showPersonDetails(1, model, request);

        verify(model).addAttribute("person", main);
        verify(model).addAttribute(eq("notes"), anyList());
        verify(model).addAttribute(eq("interactions"), anyList());
        verify(model).addAttribute(eq("interactionCounts"), anyMap());
        verify(model).addAttribute(eq("cspNonce"), eq("test-nonce"));

        assertEquals("person-details", view);
    }

    // --------------------------------------------------------------------
    @Test
    void showPersonInteractions_buildsGraphData() {
        Person main = new Person();
        main.setId(1);
        main.setName("Main");
        main.setSurname("User");

        Person other = new Person();
        other.setId(2);
        other.setName("Alice");
        other.setSurname("Wonder");

        Interaction i1 = new Interaction();
        i1.setPersonA(main);
        i1.setPersonB(other);

        Interaction i2 = new Interaction();
        i2.setPersonA(other);
        i2.setPersonB(main);

        when(personRepository.findById(1)).thenReturn(Optional.of(main));
        when(interactionRepository.findByPersonAOrPersonB(main, main))
                .thenReturn(List.of(i1, i2));

        String view = controller.showPersonInteractions(1, model, request);

        verify(model).addAttribute("person", main);
        verify(model).addAttribute(eq("interactionLabels"), anyList());
        verify(model).addAttribute(eq("interactionValues"), anyList());
        verify(model).addAttribute("personName", "Main");
        verify(model).addAttribute("personSurname", "User");
        verify(model).addAttribute("cspNonce", "test-nonce");

        assertEquals("person-interactions", view);
    }
}
