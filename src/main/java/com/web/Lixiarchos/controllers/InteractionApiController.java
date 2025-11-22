package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.model.Interaction;
import com.web.Lixiarchos.repositories.InteractionRepository;
import com.web.Lixiarchos.repositories.PersonRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("api/interactions")
public class InteractionApiController {

    private final InteractionRepository interactionRepository;
    private final PersonRepository personRepository;

    public InteractionApiController(InteractionRepository interactionRepository, PersonRepository personRepository) {
        this.interactionRepository = interactionRepository;
        this.personRepository = personRepository;
    }

    // GET all
    @GetMapping("")
    public List<Interaction> getAll() {
        return interactionRepository.findAll();
    }

    // GET by ID
    @GetMapping("/{id}")
    public Interaction getById(@PathVariable Integer id) {
        return interactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interaction not found"));
    }

    // POST create
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    public Interaction create(@Valid @RequestBody Interaction interaction) {
        validateInteraction(interaction);
        return interactionRepository.save(interaction);
    }

    // PUT update
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}")
    public void update(@PathVariable Integer id, @Valid @RequestBody Interaction interaction) {
        if (!interactionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Interaction not found");
        }
        validateInteraction(interaction);
        interaction.setId(id);
        interactionRepository.save(interaction);
    }

    // DELETE
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        if (!interactionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Interaction not found");
        }
        interactionRepository.deleteById(id);
    }

    // Validation (shared logic)
    private void validateInteraction(Interaction interaction) {
        if (interaction.getPersonA() == null || interaction.getPersonA().getId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PersonA is required");

        if (interaction.getPersonB() == null || interaction.getPersonB().getId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PersonB is required");

        if (!personRepository.existsById(interaction.getPersonA().getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PersonA does not exist");

        if (!personRepository.existsById(interaction.getPersonB().getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PersonB does not exist");

        if (interaction.getPersonA().getId().equals(interaction.getPersonB().getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PersonA and PersonB must be different");
    }
}