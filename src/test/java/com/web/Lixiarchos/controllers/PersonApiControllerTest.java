package com.web.Lixiarchos.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.repositories.PersonRepository;
// 👇 You will need to import your Enums and Date classes here as well for the setup
import com.web.Lixiarchos.enums.Language;
import com.web.Lixiarchos.enums.Sex;
import com.web.Lixiarchos.enums.Religion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
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
        // 1. Initialize ObjectMapper using standard Jackson (non-deprecated)
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        // 2. Initialize the standard Validator bean
        this.validator = new LocalValidatorFactoryBean();

        // 3. Initialize Mockito Mocks
        MockitoAnnotations.openMocks(this);

        // 4. CRITICAL FIX: Manually configure the JSON Message Converter
        // This constructor is the standard way to configure a custom ObjectMapper
        // in a non-Spring context, even if certain Spring versions flag it.
        @SuppressWarnings("deprecation")
        MappingJackson2HttpMessageConverter messageConverter =
                new MappingJackson2HttpMessageConverter(this.objectMapper);

        // 5. Build MockMvc, supplying the Validator and the Message Converter
        this.mockMvc = MockMvcBuilders.standaloneSetup(personApiController)
                .setValidator(validator)
                .setMessageConverters(messageConverter)
                .build();
    }

    // =============================================================
    // ⭐ NEW HELPER METHOD TO CREATE A VALID PERSON OBJECT ⭐
    // =============================================================
    private Person createValidPerson(String name) {
        Person p = new Person();
        p.setName(name);
        // CRITICAL: Satisfy all @NotEmpty, @Email, @Pattern constraints
        p.setSurname("Doe");
        p.setEmail("test@example.com");
        p.setTelephone("+1234567890"); // Matches the required pattern

        Set<Language> languages = new HashSet<>();
        languages.add(Language.ENGLISH); // Ensure the Set is not empty
        p.setLanguages(languages);

        // Optional fields set to prevent null issues if controller handles them
        p.setSex(Sex.MALE);
        p.setDateOfBirth(new Date());
        p.setReligion(Religion.CHRISTIAN_ORTHODOX);
        p.setIsFelon(false);
        p.setAddress("123 Test St");

        return p;
    }

    // -------------------------------------------------------------
    // GET /api/persons (Tests should pass)
    // -------------------------------------------------------------
    // ... (getAllPersons_returnsList and getPersonById tests are unchanged) ...
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
                // Note: The original test checked for p2's name, ensure it reflects the mock
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

    // -------------------------------------------------------------
    // POST /api/persons (Should now pass: 201)
    // -------------------------------------------------------------
    @Test
    void createPerson_createsAndReturnsPerson() throws Exception {
        // ⭐ Use the helper method to ensure a valid payload
        Person input = createValidPerson("New");

        Person saved = createValidPerson("New");
        saved.setId(100);

        when(personRepository.save(any(Person.class))).thenReturn(saved);

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated()) // Expected 201
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.name", is("New")));
    }

    // -------------------------------------------------------------
    // PUT /api/persons/{id}
    // -------------------------------------------------------------
    @Test
    void updatePerson_updatesWhenExists() throws Exception {
        // ⭐ Use the helper method to ensure a valid payload
        Person input = createValidPerson("Updated");
        // ID is set in the controller based on the path, but good practice to include it here too.
        input.setId(5);

        when(personRepository.existsById(5)).thenReturn(true);
        when(personRepository.save(any(Person.class))).thenReturn(input);

        mockMvc.perform(put("/api/persons/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNoContent()); // Expected 204

        verify(personRepository).save(any(Person.class));
    }

    @Test
    void updatePerson_notFound() throws Exception {
        // ⭐ Use the helper method to ensure a valid payload
        Person input = createValidPerson("Updated");
        // No ID required here, but the payload must be valid

        when(personRepository.existsById(5)).thenReturn(false);

        mockMvc.perform(put("/api/persons/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound()); // Expected 404
    }

    // -------------------------------------------------------------
    // DELETE /api/persons/{id} (Tests should pass)
    // -------------------------------------------------------------
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