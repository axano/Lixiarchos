package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.model.Interaction;
import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.repositories.InteractionRepository;
import com.web.Lixiarchos.repositories.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class InteractionApiControllerTest {

    private InteractionRepository interactionRepository;
    private PersonRepository personRepository;
    private InteractionApiController controller;

    private Person personA;
    private Person personB;
    private Interaction interaction;

    @BeforeEach
    void setUp() {
        interactionRepository = mock(InteractionRepository.class);
        personRepository = mock(PersonRepository.class);
        controller = new InteractionApiController(interactionRepository, personRepository);

        personA = new Person();
        personA.setId(1);

        personB = new Person();
        personB.setId(2);

        interaction = new Interaction();
        interaction.setPersonA(personA);
        interaction.setPersonB(personB);
    }

    // --- GET all ---
    @Test
    void getAll_ReturnsAllInteractions() {
        List<Interaction> interactions = Arrays.asList(interaction);
        when(interactionRepository.findAll()).thenReturn(interactions);

        List<Interaction> result = controller.getAll();

        assertThat(result).isEqualTo(interactions);
        verify(interactionRepository).findAll();
    }

    // --- GET by ID ---
    @Test
    void getById_Found_ReturnsInteraction() {
        when(interactionRepository.findById(1)).thenReturn(Optional.of(interaction));

        Interaction result = controller.getById(1);

        assertThat(result).isEqualTo(interaction);
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(interactionRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.getById(1));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- POST create ---
    @Test
    void create_ValidInteraction_SavesInteraction() {
        when(personRepository.existsById(1)).thenReturn(true);
        when(personRepository.existsById(2)).thenReturn(true);
        when(interactionRepository.save(interaction)).thenReturn(interaction);

        Interaction result = controller.create(interaction);

        assertThat(result).isEqualTo(interaction);
        verify(interactionRepository).save(interaction);
    }

    @Test
    void create_InvalidInteraction_SamePersons_ThrowsException() {
        interaction.setPersonB(personA);
        when(personRepository.existsById(1)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.create(interaction));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).isEqualTo("PersonA and PersonB must be different");
    }

    @Test
    void create_InvalidInteraction_PersonDoesNotExist_ThrowsException() {
        when(personRepository.existsById(1)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.create(interaction));
        assertThat(ex.getReason()).isEqualTo("PersonA does not exist");
    }

    // --- PUT update ---
    @Test
    void update_ExistingInteraction_UpdatesSuccessfully() {
        when(interactionRepository.existsById(1)).thenReturn(true);
        when(personRepository.existsById(1)).thenReturn(true);
        when(personRepository.existsById(2)).thenReturn(true);

        controller.update(1, interaction);

        ArgumentCaptor<Interaction> captor = ArgumentCaptor.forClass(Interaction.class);
        verify(interactionRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1);
    }

    @Test
    void update_NonExistingInteraction_ThrowsException() {
        when(interactionRepository.existsById(1)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.update(1, interaction));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- DELETE ---
    @Test
    void delete_ExistingInteraction_DeletesSuccessfully() {
        when(interactionRepository.existsById(1)).thenReturn(true);

        controller.delete(1);

        verify(interactionRepository).deleteById(1);
    }

    @Test
    void delete_NonExistingInteraction_ThrowsException() {
        when(interactionRepository.existsById(1)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.delete(1));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
