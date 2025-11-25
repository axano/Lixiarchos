package com.web.Lixiarchos.scheduledJobs;

import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.repositories.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;

import static com.web.Lixiarchos.services.EmailService.sendTextEmail;

@Component
public class BirthdayReminder {

    @Autowired
    private PersonRepository personRepository;

    // Runs daily at 03:10 AM
    @Scheduled(cron = "0 10 03 * * *")
    public void sendBirthdayReminders() {

        List<Person> persons = personRepository.findAll();

        LocalDate today = LocalDate.now();
        MonthDay todayMonthDay = MonthDay.from(today);

        for (Person person : persons) {

            LocalDate dob = person.getDateOfBirth();
            if (dob == null) continue; // skip if date of birth is null

            // Skip placeholder birthdays like 1000-01-01
            if (dob.getYear() <= 1000) continue;

            MonthDay birthday = MonthDay.from(dob);

            // Check if the birthday is today
            if (birthday.equals(todayMonthDay)) {
                String subject = "It is " + person.getName() + " " + person.getSurname() + "'s birthday today.";
                String body = subject;

                try {
                    sendTextEmail("perselis.e@gmail.com", subject, body);
                } catch (Exception e) {
                    System.err.println("Failed to send email: " + e.getMessage());
                }
            }
        }
    }
}
