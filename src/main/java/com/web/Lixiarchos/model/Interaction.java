package com.web.Lixiarchos.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.web.Lixiarchos.enums.InteractionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name = "interactions")
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false)
    private InteractionType interactionType;

    @NotEmpty
    @Column(length = 5000)
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateHappened;

    @ManyToOne(optional = false)
    @JoinColumn(name = "person_a_id")
    private Person personA;

    @ManyToOne(optional = false)
    @JoinColumn(name = "person_b_id")
    private Person personB;

    // ----------------------------
    // Getters and Setters
    // ----------------------------

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDateHappened() {
        return dateHappened;
    }

    public void setDateHappened(Date dateHappened) {
        this.dateHappened = dateHappened;
    }

    public Person getPersonA() {
        return personA;
    }

    public void setPersonA(Person personA) {
        this.personA = personA;
    }

    public Person getPersonB() {
        return personB;
    }

    public void setPersonB(Person personB) {
        this.personB = personB;
    }

    public InteractionType getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(InteractionType interactionType) {
        this.interactionType = interactionType;
    }
}