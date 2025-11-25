package com.web.Lixiarchos.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.repositories.PersonRepository;
import com.web.Lixiarchos.enums.Language;
import com.web.Lixiarchos.enums.Sex;
import com.web.Lixiarchos.enums.Religion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PersonApiControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonApiController personApiController;

    private Validator validator;

    @BeforeEach
    void setup() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        this.validator = new LocalValidatorFactoryBean();
        MockitoAnnotations.openMocks(this);

        MappingJackson2HttpMessageConverter messageConverter =
                new MappingJackson2HttpMessageConverter(this.objectMapper);

        this.mockMvc = MockMvcBuilders.standaloneSetup(personApiController)
                .setValidator(validator)
                .setMessageConverters(messageConverter)
                .build();
    }

    // Helper method: create valid Person with LocalDate
    private Person createValidPerson(String name) {
        Person p = new Person();
        p.setName(name);
        p.setSurname("Doe");
        p.setEmail("test@example.com");
        p.setTelephone("+1234567890");
        Set<Language> languages = new HashSet<>();
        languages.add(Language.ENGLISH);
        p.setLanguages(languages);

        p.setSex(Sex.MALE);
        // ✅ Use LocalDate for dateOfBirth
        p.setDateOfBirth(LocalDate.of(1990, 1, 1));
        p.setReligion(Religion.CHRISTIAN_ORTHODOX);
        p.setIsFelon(false);
        p.setAddress("123 Test St");

        return p;
    }

    @Test
    void getAllPersons_returnsList() throws Exception {
        Person p1 = createValidPerson("John");
        p1.setId(1);
        Person p2 = createValidPerson("Jane");
        p2.setId(2);

        when(personRepository.findAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].name", is("Jane")));
    }

    @Test
    void getPersonById_returnsPerson() throws Exception {
        Person person = createValidPerson("Alice");
        person.setId(5);

        when(personRepository.findById(5)).thenReturn(Optional.of(person));

        mockMvc.perform(get("/api/persons/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.name", is("Alice")));
    }

    @Test
    void getPersonById_notFound() throws Exception {
        when(personRepository.findById(5)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/persons/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPerson_createsAndReturnsPerson() throws Exception {
        Person input = createValidPerson("New");
        Person saved = createValidPerson("New");
        saved.setId(100);

        when(personRepository.save(any(Person.class))).thenReturn(saved);

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.name", is("New")));
    }

    @Test
    void updatePerson_updatesWhenExists() throws Exception {
        Person input = createValidPerson("Updated");
        input.setId(5);

        when(personRepository.existsById(5)).thenReturn(true);
        when(personRepository.save(any(Person.class))).thenReturn(input);

        mockMvc.perform(put("/api/persons/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNoContent());

        verify(personRepository).save(any(Person.class));
    }

    @Test
    void updatePerson_notFound() throws Exception {
        Person input = createValidPerson("Updated");

        when(personRepository.existsById(5)).thenReturn(false);

        mockMvc.perform(put("/api/persons/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePerson_deletesWhenExists() throws Exception {
        when(personRepository.existsById(9)).thenReturn(true);
        doNothing().when(personRepository).deleteById(9);

        mockMvc.perform(delete("/api/persons/9"))
                .andExpect(status().isNoContent());

        verify(personRepository).deleteById(9);
    }

    @Test
    void deletePerson_notFound() throws Exception {
        when(personRepository.existsById(9)).thenReturn(false);

        mockMvc.perform(delete("/api/persons/9"))
                .andExpect(status().isNotFound());
    }
}
