package com.web.Lixiarchos.repositories;

import com.web.Lixiarchos.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer> {
    // JpaRepository provides CRUD methods automatically:
    // findAll(), findById(), save(), deleteById(), etc.
}