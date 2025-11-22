package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.model.Note;
import com.web.Lixiarchos.model.exceptions.NoteNotFoundException;
import com.web.Lixiarchos.repositories.NoteRepository;
import com.web.Lixiarchos.repositories.PersonRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("api/notes")
public class NoteApiController {

    private final NoteRepository noteRepository;
    private final PersonRepository personRepository;

    public NoteApiController(NoteRepository noteRepository, PersonRepository personRepository) {
        this.noteRepository = noteRepository;
        this.personRepository = personRepository;
    }


    // 🔹 GET all notes
    @GetMapping("")
    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    // 🔹 GET note by ID
    @GetMapping("/{id}")
    public Note getNoteById(@Valid @PathVariable Integer id) {
        return noteRepository.findById(id)
                .orElseThrow(NoteNotFoundException::new);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    public Note createNote(@Valid @RequestBody Note note) {
        // ✅ Ensure referenced person exists
        if (note.getPerson() == null || note.getPerson().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Person ID is required");
        }

        if (!personRepository.existsById(note.getPerson().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Person with ID " + note.getPerson().getId() + " does not exist");
        }

        return noteRepository.save(note);
    }

    // 🔹 PUT (update existing note)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping("/{id}")
    public void updateNote(@Valid @RequestBody Note note, @PathVariable Integer id) {
        if (!noteRepository.existsById(id)) {
            throw new NoteNotFoundException();
        }
        note.setId(id);
        noteRepository.save(note);
    }

    // 🔹 DELETE note by ID
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteNote(@Valid @PathVariable Integer id) {
        if (!noteRepository.existsById(id)) {
            throw new NoteNotFoundException();
        }
        noteRepository.deleteById(id);
    }

    // 🔹 GET all notes for a specific person
    @GetMapping("/person/{personId}")
    public List<Note> getNotesByPersonId(@PathVariable Integer personId) {
        // Optional: validate person exists
        if (!personRepository.existsById(personId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Person with ID " + personId + " does not exist");
        }

        return noteRepository.findByPersonId(personId);
    }
}
