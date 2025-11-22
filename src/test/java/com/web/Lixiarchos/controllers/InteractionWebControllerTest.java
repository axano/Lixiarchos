package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.enums.InteractionType;
import com.web.Lixiarchos.model.Interaction;
import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.repositories.InteractionRepository;
import com.web.Lixiarchos.repositories.PersonRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class InteractionWebControllerTest {

    private InteractionRepository interactionRepository;
    private PersonRepository personRepository;
    private InteractionWebController controller;
    private Model model;
    private HttpServletRequest request;

    private Person personA;
    private Person personB;
    private Interaction interaction;

    @BeforeEach
    void setUp() {
        interactionRepository = mock(InteractionRepository.class);
        personRepository = mock(PersonRepository.class);
        model = mock(Model.class);
        request = mock(HttpServletRequest.class);

        controller = new InteractionWebController(interactionRepository, personRepository);

        personA = new Person();
        personA.setId(1);

        personB = new Person();
        personB.setId(2);

        interaction = new Interaction();
        interaction.setId(1);
        interaction.setPersonA(personA);
        interaction.setPersonB(personB);
    }

    // --- LIST all ---
    @Test
    void listAll_AddsInteractionsToModel() {
        List<Interaction> interactions = Arrays.asList(interaction);
        when(interactionRepository.findAll()).thenReturn(interactions);

        String view = controller.listAll(model, request);

        assertThat(view).isEqualTo("interactions");
        verify(model).addAttribute("interactions", interactions);
        verify(model).addAttribute(eq("cspNonce"), anyString());
    }

    // --- SHOW CREATE FORM ---
    @Test
    void showCreateForm_WithPersonId_PreloadsPerson() {
        when(personRepository.findById(1)).thenReturn(Optional.of(personA));
        when(personRepository.findAll()).thenReturn(Arrays.asList(personA, personB));

        String view = controller.showCreateForm(1, model, request);

        assertThat(view).isEqualTo("interaction-form");
        verify(model).addAttribute(eq("interaction"), any(Interaction.class));
        verify(model).addAttribute("persons", Arrays.asList(personA, personB));
        verify(model).addAttribute("types", InteractionType.values());
        verify(model).addAttribute(eq("cspNonce"), anyString());
    }

    @Test
    void showCreateForm_InvalidPersonId_ThrowsException() {
        when(personRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.showCreateForm(99, model, request));
    }

    // --- CREATE submit ---
    @Test
    void create_SavesInteractionAndRedirects() {
        String view = controller.create(interaction);

        assertThat(view).isEqualTo("redirect:/interactions");
        verify(interactionRepository).save(interaction);
    }

    // --- SHOW EDIT FORM ---
    @Test
    void showEditForm_ValidId_AddsInteractionToModel() {
        when(interactionRepository.findById(1)).thenReturn(Optional.of(interaction));
        when(personRepository.findAll()).thenReturn(Arrays.asList(personA, personB));

        String view = controller.showEditForm(1, model, request);

        assertThat(view).isEqualTo("interaction-form");
        verify(model).addAttribute("interaction", interaction);
        verify(model).addAttribute("persons", Arrays.asList(personA, personB));
        verify(model).addAttribute("types", InteractionType.values());
        verify(model).addAttribute(eq("cspNonce"), anyString());
    }

    @Test
    void showEditForm_InvalidId_ThrowsException() {
        when(interactionRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.showEditForm(99, model, request));
    }

    // --- UPDATE submit ---
    @Test
    void update_SavesInteractionAndRedirects() {
        String view = controller.update(1, interaction);

        assertThat(view).isEqualTo("redirect:/interactions");

        ArgumentCaptor<Interaction> captor = ArgumentCaptor.forClass(Interaction.class);
        verify(interactionRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1);
    }

    // --- DELETE ---
    @Test
    void delete_DeletesInteractionAndRedirects() {
        String view = controller.delete(1);

        assertThat(view).isEqualTo("redirect:/interactions");
        verify(interactionRepository).deleteById(1);
    }

    // --- LIST interactions for a person ---
    @Test
    void listByPerson_ValidPerson_AddsInteractionsToModel() {
        List<Interaction> interactions = Arrays.asList(interaction);
        when(personRepository.findById(1)).thenReturn(Optional.of(personA));
        when(interactionRepository.findByPersonAIdOrPersonBId(1,1)).thenReturn(interactions);

        String view = controller.listByPerson(1, model, request);

        assertThat(view).isEqualTo("person-interactions");
        verify(model).addAttribute("person", personA);
        verify(model).addAttribute("interactions", interactions);
        verify(model).addAttribute(eq("cspNonce"), anyString());
    }

    @Test
    void listByPerson_InvalidPerson_ThrowsException() {
        when(personRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.listByPerson(99, model, request));
    }

    @Test
    void showPersonInteractions_AddsInteractionCounts() {
        List<Interaction> interactions = Arrays.asList(interaction);
        when(personRepository.findById(1)).thenReturn(Optional.of(personA));
        when(interactionRepository.findByPersonAOrPersonB(personA, personA)).thenReturn(interactions);

        String view = controller.showPersonInteractions(1, model, request);

        assertThat(view).isEqualTo("person-interactions");
        verify(model).addAttribute("person", personA);
        verify(model).addAttribute("interactions", interactions);
        verify(model).addAttribute(eq("interactionCounts"), anyMap());
        verify(model).addAttribute(eq("cspNonce"), anyString());
    }
}
