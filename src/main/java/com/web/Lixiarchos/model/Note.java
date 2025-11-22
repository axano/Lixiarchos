package com.web.Lixiarchos.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Table(name = "Notes", schema = "lixiarchos")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @PositiveOrZero
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Column(length = 5000)
    @NotEmpty
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date dateRegistered;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getDateRegistered() { return dateRegistered; }
    public void setDateRegistered(Date dateRegistered) { this.dateRegistered = dateRegistered; }
}