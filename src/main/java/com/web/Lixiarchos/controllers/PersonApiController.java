package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.model.exceptions.PersonNotFoundException;
import com.web.Lixiarchos.repositories.PersonRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/persons")
public class PersonApiController {

    private final PersonRepository personRepository;

    public PersonApiController(PersonRepository personRepository){
        this.personRepository = personRepository;
    }

    @GetMapping("")
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    @GetMapping("/{id}")
    public Person getPersonById(@Valid @PathVariable Integer id) {
        return personRepository.findById(id)
                .orElseThrow(PersonNotFoundException::new);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    public Person createPerson(@Valid @RequestBody Person person) {
        // ID will be auto-generated if using @GeneratedValue in your entity
        return personRepository.save(person);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}")
    public void updatePerson(@Valid @RequestBody Person person, @PathVariable Integer id) {
        if (!personRepository.existsById(id)) {
            throw new PersonNotFoundException();
        }
        person.setId(id);
        personRepository.save(person);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@Valid @PathVariable Integer id) {
        if (!personRepository.existsById(id)) {
            throw new PersonNotFoundException();
        }
        personRepository.deleteById(id);
    }
}