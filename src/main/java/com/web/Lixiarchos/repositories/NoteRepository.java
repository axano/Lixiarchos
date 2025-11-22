package com.web.Lixiarchos.repositories;

import com.web.Lixiarchos.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Integer> {
    // Fetch all notes for a given person ID
    List<Note> findByPersonId(Integer personId);
}
