package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.model.Note;
import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.repositories.NoteRepository;
import com.web.Lixiarchos.repositories.PersonRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
@RequestMapping("/notes")
public class NoteWebController {

    private final NoteRepository noteRepository;
    private final PersonRepository personRepository;

    public NoteWebController(NoteRepository noteRepository, PersonRepository personRepository) {
        this.noteRepository = noteRepository;
        this.personRepository = personRepository;
    }

    // Show form to add a note for a specific person
    @GetMapping("/new/{personId}")
    public String showNoteForm(@PathVariable Integer personId, Model model) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid person ID"));

        Note note = new Note();
        note.setPerson(person);
        note.setDateRegistered(new Date());

        model.addAttribute("note", note);
        model.addAttribute("personName", person.getName() + " " + person.getSurname());
        model.addAttribute("personId", personId);  // used in hidden field
        return "note-form";
    }

    // Handle form submission
    @PostMapping("/save")
    public String saveNote(@Valid @ModelAttribute Note note) {
        noteRepository.save(note);
        return "redirect:/persons/details/" + note.getPerson().getId();
    }

    @PostMapping("")
    public String createNote(@ModelAttribute Note note, @RequestParam Integer personId) {
        // attach person entity
        note.setPerson(personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid person ID")));
        noteRepository.save(note);
        return "redirect:/persons/details/" + personId;
    }
    // Show form to edit an existing note
    @GetMapping("/edit/{noteId}")
    public String showEditNoteForm(@PathVariable Integer noteId, Model model) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid note ID"));
        model.addAttribute("note", note);
        model.addAttribute("personId", note.getPerson().getId());
        return "note-form";
    }

    // Handle form submission for updating a note
    @PostMapping("/update/{noteId}")
    public String updateNote(@PathVariable Integer noteId,
                             @ModelAttribute Note note,
                             @RequestParam Integer personId) {
        Note existing = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid note ID"));
        existing.setContent(note.getContent());
        existing.setDateRegistered(note.getDateRegistered());
        // Keep person association
        existing.setPerson(personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid person ID")));
        noteRepository.save(existing);
        return "redirect:/persons/details/" + personId;
    }

    // Optional: delete note
    @GetMapping("/delete/{noteId}")
    public String deleteNote(@PathVariable Integer noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid note ID"));
        Integer personId = note.getPerson().getId();
        noteRepository.delete(note);
        return "redirect:/persons/details/" + personId;
    }
}
