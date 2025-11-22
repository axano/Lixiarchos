package com.web.Lixiarchos.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.web.Lixiarchos.enums.Language;
import com.web.Lixiarchos.enums.Religion;
import com.web.Lixiarchos.enums.Sex;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "persons", schema = "lixiarchos")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @PositiveOrZero
    private Integer id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String surname;
    private Sex sex;
    private String vocation;
    private String address;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dateOfBirth;
    private Religion religion;
    @JsonProperty("isFelon")
    @Column(name = "is_felon", nullable = false)
    private Boolean isFelon = false;
    private String origin;
    @NotEmpty
    @ElementCollection(targetClass = Language.class)
    @Enumerated(EnumType.STRING)
    private Set<Language> languages = new HashSet<>();
    @NotEmpty
    @Email
    private String email;
    @NotEmpty
    @Pattern(regexp="\\+?[0-9\\- ]{7,15}", message="Invalid phone number")
    private String telephone;


    public Person(Integer id, String name, String surname, Sex sex, String vocation, String address, Date dateOfBirth, Religion religion, Boolean isFelon, String origin, Set<Language> languages, String email, String telephone ){
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.sex = sex;
        this.vocation = vocation;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.religion = religion;
        this.isFelon = isFelon;
        this.origin = origin;
        this.languages = languages;
        this.email = email;
        this.telephone = telephone;
    }

    public Person(Integer id) {
        HashSet<Language> languages = new HashSet<>();
        languages.add(Language.ENGLISH);
        this.id = id;
        this.name = "test";
        this.surname = "test";
        this.sex = Sex.MALE;
        this.vocation = "test";
        this.address = "test";
        this.dateOfBirth = new Date();
        this.religion = Religion.CHRISTIAN_ORTHODOX;
        this.isFelon = false;
        this.origin = "test";
        this.languages = languages;
        this.email = "email@email.com";
        this.telephone = "+1234567890";
    }

    public Person() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public String getVocation() {
        return vocation;
    }

    public void setVocation(String vocation) {
        this.vocation = vocation;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Religion getReligion() {
        return religion;
    }

    public void setReligion(Religion religion) {
        this.religion = religion;
    }

    @JsonProperty("isFelon")
    public Boolean getIsFelon() {
        return isFelon;
    }
    //public Boolean getFelon() {return isFelon;}

    //public void setFelon(Boolean felon) {isFelon = felon;}
    @JsonProperty("isFelon")
    public void setIsFelon(Boolean felon) {
        isFelon = felon;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Set<Language> getLanguages() { return languages; }

    public void setLanguages(Set<Language> languages) { this.languages = languages; }

    @Transient
    public String getLanguagesString() {
        if (languages == null || languages.isEmpty()) return "";
        return languages.stream()
                .map(Language::name)
                .collect(Collectors.joining(","));
    }

    public void setLanguagesString(String languagesString) {
        if (languagesString != null && !languagesString.trim().isEmpty()) {
            String[] arr = languagesString.split("\\s*,\\s*");
            Set<Language> langSet = new HashSet<>();
            for (String s : arr) {
                try {
                    langSet.add(Language.valueOf(s));
                } catch (IllegalArgumentException e) {
                    // ignore invalid values
                }
            }
            languages = langSet;
        } else {
            languages = new HashSet<>();
        }
    }

    public Boolean getFelon() {
        return isFelon;
    }

    public void setFelon(Boolean felon) {
        isFelon = felon;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", sex=" + sex +
                ", vocation='" + vocation + '\'' +
                ", address='" + address + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", religion=" + religion +
                ", isFelon=" + isFelon +
                ", origin='" + origin + '\'' +
                ", languages=" + (languages == null ? "[]" :
                languages.stream().map(Language::name).collect(Collectors.joining(","))) +
                ", email=" + email +
                ", telephone=" + telephone +
                '}';
    }
}