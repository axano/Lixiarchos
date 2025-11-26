package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.enums.Language;
import com.web.Lixiarchos.model.Interaction;
import com.web.Lixiarchos.model.Note;
import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.model.exceptions.PersonNotFoundException;
import com.web.Lixiarchos.repositories.InteractionRepository;
import com.web.Lixiarchos.repositories.NoteRepository;
import com.web.Lixiarchos.repositories.PersonRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Base64;

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

    private String generateCspNonce(HttpServletRequest request) {
        String nonce = (String) request.getAttribute("cspNonce");
        if (nonce == null) {
            nonce = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        }
        return nonce;
    }

    @GetMapping
    public String showAllPersons(Model model, HttpServletRequest request) {
        model.addAttribute("persons", personRepository.findAll());
        model.addAttribute("cspNonce", generateCspNonce(request));
        return "persons";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, HttpServletRequest request) {
        Person person = personRepository.findById(id)
                .orElseThrow(PersonNotFoundException::new);
        model.addAttribute("person", person);
        model.addAttribute("sexOptions", com.web.Lixiarchos.enums.Sex.values());
        model.addAttribute("religionOptions", com.web.Lixiarchos.enums.Religion.values());
        model.addAttribute("languageOptions", Language.values());
        model.addAttribute("cspNonce", generateCspNonce(request));
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
    public String showCreateForm(Model model, HttpServletRequest request) {
        Person person = new Person();
        person.setEmail("empty@gmail.com");
        person.setTelephone("+32 000 00 00 00");
        person.setDateOfBirth(LocalDate.of(1000, 1, 1));
        model.addAttribute("person", person);
        model.addAttribute("sexOptions", com.web.Lixiarchos.enums.Sex.values());
        model.addAttribute("religionOptions", com.web.Lixiarchos.enums.Religion.values());
        model.addAttribute("languageOptions", Language.values());
        model.addAttribute("cspNonce", generateCspNonce(request));
        return "person-form";
    }

    @PostMapping("/update/{id}")
    public String updatePerson(@PathVariable Integer id, @ModelAttribute Person person) {
        person.setId(id);
        personRepository.save(person);
        return "redirect:/persons";
    }

    @GetMapping("/delete/{id}")
    public String deletePerson(@PathVariable Integer id) {
        personRepository.deleteById(id);
        return "redirect:/persons";
    }

    @GetMapping("/details/{id}")
    public String showPersonDetails(@PathVariable Integer id, Model model, HttpServletRequest request) {
        Person person = personRepository.findById(id)
                .orElseThrow(PersonNotFoundException::new);
        model.addAttribute("person", person);

        List<Note> notes = noteRepository.findByPersonId(id);
        model.addAttribute("notes", notes);

        List<Interaction> interactions = interactionRepository.findByPersonAOrPersonB(person, person);
        model.addAttribute("interactions", interactions);

        Map<String, Long> interactionCounts = interactions.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getPersonA().equals(person) ?
                                i.getPersonB().getName() + " " + i.getPersonB().getSurname() :
                                i.getPersonA().getName() + " " + i.getPersonA().getSurname(),
                        Collectors.counting()
                ));
        model.addAttribute("interactionCounts", interactionCounts);

        model.addAttribute("cspNonce", generateCspNonce(request));
        return "person-details";
    }

    @GetMapping("/interactions/{personId}")
    public String showPersonInteractions(@PathVariable Integer personId, Model model, HttpServletRequest request) {
        Person person = personRepository.findById(personId)
                .orElseThrow(PersonNotFoundException::new);

        List<Interaction> interactions = interactionRepository.findByPersonAOrPersonB(person, person);

        Map<Person, Long> interactionCounts = interactions.stream()
                .map(i -> i.getPersonA().equals(person) ? i.getPersonB() : i.getPersonA())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

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
        model.addAttribute("cspNonce", generateCspNonce(request));

        return "person-interactions";
    }
}
