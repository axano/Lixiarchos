package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.model.Interaction;
import com.web.Lixiarchos.enums.InteractionType;
import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.repositories.InteractionRepository;
import com.web.Lixiarchos.repositories.PersonRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
    public String listAll(Model model) {
        model.addAttribute("interactions", interactionRepository.findAll());
        return "interactions"; // interactions.html
    }

    // CREATE form
    @GetMapping("/new")
    public String showCreateForm(@RequestParam(required = false) Integer personId,
                                 Model model) {

        Interaction interaction = new Interaction();
        interaction.setDateHappened(new java.util.Date());

        if (personId != null) {
            Person selected = personRepository.findById(personId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid person ID"));

            // Preselect the person as personA (or personB depending on your preference)
            interaction.setPersonA(selected);
        }

        model.addAttribute("interaction", interaction);
        model.addAttribute("persons", personRepository.findAll());
        model.addAttribute("types", InteractionType.values());

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
    public String showEditForm(@PathVariable Integer id, Model model) {
        Interaction interaction = interactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid interaction ID"));

        model.addAttribute("interaction", interaction);
        model.addAttribute("persons", personRepository.findAll());
        model.addAttribute("types", InteractionType.values());

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

    // LIST interactions *for one person*
    @GetMapping("/person/{personId}")
    public String listByPerson(@PathVariable Integer personId, Model model) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid person ID"));

        model.addAttribute("person", person);
        model.addAttribute("interactions", interactionRepository.findByPersonAIdOrPersonBId(personId, personId));

        return "person-interactions"; // person-interactions.html
    }
    @GetMapping("/interactions/{personId}")
    public String showPersonInteractions(@PathVariable Integer personId, Model model) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid person ID: " + personId));

        model.addAttribute("person", person);

        // Get all interactions for this person
        List<Interaction> interactions = interactionRepository.findByPersonAOrPersonB(person, person);
        model.addAttribute("interactions", interactions);

        // Build a map of "other person" -> count of interactions
        Map<Person, Long> interactionCounts = interactions.stream()
                .map(i -> i.getPersonA().getId().equals(person.getId()) ? i.getPersonB() : i.getPersonA())
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()));

        model.addAttribute("interactionCounts", interactionCounts);

        return "person-interactions";
    }
}