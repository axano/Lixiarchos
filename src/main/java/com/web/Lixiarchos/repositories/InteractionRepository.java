package com.web.Lixiarchos.repositories;

import com.web.Lixiarchos.model.Interaction;
import com.web.Lixiarchos.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InteractionRepository extends JpaRepository<Interaction, Integer> {

    @Query("SELECT i FROM Interaction i WHERE i.personA.id = :id OR i.personB.id = :id")
    List<Interaction> findByPersonAIdOrPersonBId(Integer id, Integer id2);

    List<Interaction> findByPersonAOrPersonB(Person personA, Person personB);
}
