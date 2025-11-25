package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.model.Interaction;
import com.web.Lixiarchos.enums.InteractionType;
import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.repositories.InteractionRepository;
import com.web.Lixiarchos.repositories.PersonRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/interactions")
public class InteractionWebController {

    private final InteractionRepository interactionRepository;
    private final PersonRepository personRepository;

    public InteractionWebController(InteractionRepository interactionRepository, PersonRepository personRepository) {
        this.interactionRepository = interactionRepository;
        this.personRepository = personRepository;
    }

    // LIST all interactions
    @GetMapping("")
    public String listAll(Model model, HttpServletRequest request) {
        model.addAttribute("interactions", interactionRepository.findAll());
        model.addAttribute("cspNonce", getOrCreateNonce(request));
        return "interactions";
    }

    // CREATE form
    @GetMapping("/new")
    public String showCreateForm(@RequestParam(required = false) Integer personId,
                                 Model model,
                                 HttpServletRequest request) {

        Interaction interaction = new Interaction();
        interaction.setDateHappened(new Date());

        if (personId != null) {
            Person selected = personRepository.findById(personId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid person ID"));
            interaction.setPersonA(selected);
        }

        model.addAttribute("interaction", interaction);
        model.addAttribute("persons", personRepository.findAll());
        model.addAttribute("types", InteractionType.values());
        model.addAttribute("cspNonce", getOrCreateNonce(request));

        return "interaction-form";
    }

    // CREATE submit
    @PostMapping("")
    public String create(@ModelAttribute Interaction interaction) {
        interactionRepository.save(interaction);
        return "redirect:/interactions";
    }

    // EDIT form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, HttpServletRequest request) {
        Interaction interaction = interactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid interaction ID"));

        model.addAttribute("interaction", interaction);
        model.addAttribute("persons", personRepository.findAll());
        model.addAttribute("types", InteractionType.values());
        model.addAttribute("cspNonce", getOrCreateNonce(request));

        return "interaction-form";
    }

    // UPDATE submit
    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute Interaction interaction) {
        interaction.setId(id);
        interactionRepository.save(interaction);
        return "redirect:/interactions";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        interactionRepository.deleteById(id);
        return "redirect:/interactions";
    }

    // LIST interactions for one person
    @GetMapping("/person/{personId}")
    public String listByPerson(@PathVariable Integer personId, Model model, HttpServletRequest request) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid person ID"));

        model.addAttribute("person", person);
        model.addAttribute("interactions", interactionRepository.findByPersonAIdOrPersonBId(personId, personId));
        model.addAttribute("cspNonce", getOrCreateNonce(request));

        return "person-interactions";
    }

    // SHOW interactions with "other person" counts (for charts)
    @GetMapping("/interactions/{personId}")
    public String showPersonInteractions(@PathVariable Integer personId, Model model, HttpServletRequest request) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid person ID: " + personId));

        model.addAttribute("person", person);

        List<Interaction> interactions = interactionRepository.findByPersonAOrPersonB(person, person);
        model.addAttribute("interactions", interactions);

        Map<Person, Long> interactionCounts = interactions.stream()
                .map(i -> i.getPersonA().getId().equals(person.getId()) ? i.getPersonB() : i.getPersonA())
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()));
        model.addAttribute("interactionCounts", interactionCounts);

        model.addAttribute("cspNonce", getOrCreateNonce(request));

        return "person-interactions";
    }

    // HELPER: Get or generate CSP nonce
    private String getOrCreateNonce(HttpServletRequest request) {
        String nonce = (String) request.getAttribute("cspNonce");
        if (nonce == null) {
            nonce = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        }
        return nonce;
    }
}
