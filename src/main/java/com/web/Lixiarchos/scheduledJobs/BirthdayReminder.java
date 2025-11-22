package com.web.Lixiarchos.scheduledJobs;

import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.repositories.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
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

        for (Person person : persons) {
            Calendar cal = Calendar.getInstance();

            Date dob = person.getDateOfBirth();
            cal.setTime(dob);
            if(  cal.get(Calendar.MONTH) == Calendar.JANUARY && cal.get(Calendar.DAY_OF_MONTH) == 1) continue;

            LocalDate birthDate = dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            MonthDay birthday = MonthDay.from(birthDate);
            MonthDay todayMonthDay = MonthDay.from(today);

            // Check if the birthday is today
            if (birthday.equals(todayMonthDay)) {
                String subject = "It is " + person.getName() + " " + person.getSurname() + "'s birthday today.";
                String body = subject;

                try {
                    sendTextEmail("perselis.e@gmail.com", subject, body);
                } catch (Exception e) {
                    System.err.println("Failed to send email " + e.getMessage());
                }
            }
        }
    }
}
