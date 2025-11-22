package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.enums.Language;
import com.web.Lixiarchos.model.Interaction;
import com.web.Lixiarchos.model.Note;
import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.repositories.InteractionRepository;
import com.web.Lixiarchos.repositories.NoteRepository;
import com.web.Lixiarchos.repositories.PersonRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/persons")
public class PersonWebController {

    private final PersonRepository personRepository;
    private final NoteRepository noteRepository;
    private final InteractionRepository interactionRepository;

    public PersonWebController(PersonRepository personRepository,
                               NoteRepository noteRepository,
                               InteractionRepository interactionRepository) {
        this.personRepository = personRepository;
        this.noteRepository = noteRepository;
        this.interactionRepository = interactionRepository;
    }

    @GetMapping
    public String showAllPersons(Model model) {
        model.addAttribute("persons", personRepository.findAll());
        return "persons";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid person ID"));
        model.addAttribute("person", person);
        model.addAttribute("sexOptions", com.web.Lixiarchos.enums.Sex.values());
        model.addAttribute("religionOptions", com.web.Lixiarchos.enums.Religion.values());
        model.addAttribute("languageOptions", Language.values()); // NEW
        return "person-form";
    }

    @PostMapping
    public String createPerson(@ModelAttribute Person person) {
        if (person.getIsFelon() == null) {
            person.setIsFelon(false);
        }
        personRepository.save(person);
        return "redirect:/persons";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Person person = new Person();
        person.setEmail("empty@gmail.com"); // Default email to avoid validation issues
        person.setTelephone("+32 000 00 00 00"); // Default email to avoid validation issues
        person.setDateOfBirth(new Date("01/01/1000")); // Default email to avoid validation issues

        model.addAttribute("person", person);
        model.addAttribute("sexOptions", com.web.Lixiarchos.enums.Sex.values());
        model.addAttribute("religionOptions", com.web.Lixiarchos.enums.Religion.values());
        model.addAttribute("languageOptions", Language.values());
        return "person-form";
    }

    @PostMapping("/update/{id}")
    public String updatePerson(@PathVariable Integer id, @ModelAttribute Person person) {
        person.setId(id);  // ensure ID is set
        personRepository.save(person);
        return "redirect:/persons";
    }

    @GetMapping("/delete/{id}")
    public String deletePerson(@PathVariable Integer id) {
        personRepository.deleteById(id);
        return "redirect:/persons";
    }

    @GetMapping("/details/{id}")
    public String showPersonDetails(@PathVariable Integer id, Model model) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid person ID: " + id));
        model.addAttribute("person", person);

        // Notes
        List<Note> notes = noteRepository.findByPersonId(id);
        model.addAttribute("notes", notes);

        // Interactions for this person
        List<Interaction> interactions = interactionRepository.findByPersonAOrPersonB(person, person);
        model.addAttribute("interactions", interactions);

        // Prepare interaction counts for chart (other person -> # interactions)
        Map<String, Long> interactionCounts = interactions.stream()
                .collect(Collectors.groupingBy(
                        i -> {
                            if (i.getPersonA().getId().equals(person.getId())) {
                                return i.getPersonB().getName() + " " + i.getPersonB().getSurname();
                            } else {
                                return i.getPersonA().getName() + " " + i.getPersonA().getSurname();
                            }
                        },
                        Collectors.counting()
                ));
        model.addAttribute("interactionCounts", interactionCounts);

        return "person-details";
    }

    @GetMapping("/interactions/{personId}")
    public String showPersonInteractions(@PathVariable Integer personId, Model model) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid person ID"));

        List<Interaction> interactions = interactionRepository.findByPersonAOrPersonB(person, person);

        // Build a map: Other person -> count
        Map<Person, Long> interactionCounts = interactions.stream()
                .map(i -> i.getPersonA().equals(person) ? i.getPersonB() : i.getPersonA())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Convert to Thymeleaf-friendly lists
        List<String> labels = interactionCounts.keySet().stream()
                .map(p -> p.getName() + " " + p.getSurname())
                .collect(Collectors.toList());

        List<Long> values = new ArrayList<>(interactionCounts.values());

        model.addAttribute("person", person);
        model.addAttribute("interactions", interactions);
        model.addAttribute("interactionLabels", labels);
        model.addAttribute("interactionValues", values);
        model.addAttribute("personName", person.getName());
        model.addAttribute("personSurname", person.getSurname());


        return "person-interactions";
    }


}
