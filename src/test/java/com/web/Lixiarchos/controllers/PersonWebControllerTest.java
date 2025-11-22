package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.enums.Language;
import com.web.Lixiarchos.model.Interaction;
import com.web.Lixiarchos.model.Note;
import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.repositories.InteractionRepository;
import com.web.Lixiarchos.repositories.NoteRepository;
import com.web.Lixiarchos.repositories.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.ui.Model;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonWebControllerTest {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private InteractionRepository interactionRepository;

    @Mock
    private Model model;

    @InjectMocks
    private PersonWebController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // -------------------------------------------------------------
    // GET /persons
    // -------------------------------------------------------------
    @Test
    void showAllPersons_returnsViewAndModel() {
        List<Person> persons = List.of(new Person(), new Person());
        when(personRepository.findAll()).thenReturn(persons);

        String result = controller.showAllPersons(model);

        verify(model).addAttribute("persons", persons);
        assertEquals("persons", result);
    }

    // -------------------------------------------------------------
    // GET /persons/edit/{id}
    // -------------------------------------------------------------
    @Test
    void showEditForm_validId_returnsForm() {
        Person p = new Person();
        p.setId(1);
        when(personRepository.findById(1)).thenReturn(Optional.of(p));

        String view = controller.showEditForm(1, model);

        verify(model).addAttribute("person", p);
        verify(model).addAttribute(eq("languageOptions"), eq(Language.values()));
        assertEquals("person-form", view);
    }

    @Test
    void showEditForm_invalidId_throwsException() {
        when(personRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> controller.showEditForm(1, model));
    }

    // -------------------------------------------------------------
    // POST /persons
    // -------------------------------------------------------------
    @Test
    void createPerson_setsDefaultFelon_whenNull() {
        Person p = new Person();
        p.setIsFelon(null);

        controller.createPerson(p);

        assertFalse(p.getIsFelon());
        verify(personRepository).save(p);
    }

    @Test
    void createPerson_redirectsAfterSaving() {
        Person p = new Person();
        p.setIsFelon(false);

        String redirect = controller.createPerson(p);
        assertEquals("redirect:/persons", redirect);
    }

    // -------------------------------------------------------------
    // GET /persons/new
    // -------------------------------------------------------------
    @Test
    void showCreateForm_setsDefaultsAndModel() {
        String view = controller.showCreateForm(model);

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        verify(model).addAttribute(eq("person"), captor.capture());
        Person passed = captor.getValue();

        assertEquals("empty@gmail.com", passed.getEmail());
        assertNotNull(passed.getDateOfBirth());
        assertEquals("person-form", view);
    }

    // -------------------------------------------------------------
    // POST /persons/update/{id}
    // -------------------------------------------------------------
    @Test
    void updatePerson_setsIdAndSaves() {
        Person p = new Person();
        String redirect = controller.updatePerson(5, p);

        assertEquals(5, p.getId());
        verify(personRepository).save(p);
        assertEquals("redirect:/persons", redirect);
    }

    // -------------------------------------------------------------
    // GET /persons/delete/{id}
    // -------------------------------------------------------------
    @Test
    void deletePerson_deletesAndRedirects() {
        String redirect = controller.deletePerson(10);

        verify(personRepository).deleteById(10);
        assertEquals("redirect:/persons", redirect);
    }

    // -------------------------------------------------------------
    // GET /persons/details/{id}
    // -------------------------------------------------------------
    @Test
    void showPersonDetails_loadsNotesAndInteractionsAndCounts() {
        Person main = new Person();
        main.setId(1);
        main.setName("John");
        main.setSurname("Smith");

        Person other = new Person();
        other.setId(2);
        other.setName("Jane");
        other.setSurname("Doe");

        Interaction interaction = new Interaction();
        interaction.setPersonA(main);
        interaction.setPersonB(other);

        when(personRepository.findById(1)).thenReturn(Optional.of(main));
        when(noteRepository.findByPersonId(1)).thenReturn(List.of(new Note()));
        when(interactionRepository.findByPersonAOrPersonB(main, main))
                .thenReturn(List.of(interaction));

        String view = controller.showPersonDetails(1, model);

        verify(model).addAttribute("person", main);
        verify(model).addAttribute(eq("notes"), anyList());
        verify(model).addAttribute(eq("interactions"), anyList());
        verify(model).addAttribute(eq("interactionCounts"), anyMap());
        assertEquals("person-details", view);
    }

    // -------------------------------------------------------------
    // GET /persons/interactions/{personId}
    // -------------------------------------------------------------
    @Test
    void showPersonInteractions_buildsLabelsAndValues() {
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

        String view = controller.showPersonInteractions(1, model);

        verify(model).addAttribute("person", main);
        verify(model).addAttribute(eq("interactionLabels"), anyList());
        verify(model).addAttribute(eq("interactionValues"), anyList());
        assertEquals("person-interactions", view);
    }
}
