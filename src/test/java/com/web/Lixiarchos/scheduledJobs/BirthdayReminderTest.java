package com.web.Lixiarchos.scheduledJobs;

import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.repositories.PersonRepository;
import com.web.Lixiarchos.services.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BirthdayReminderTest {

    @Mock private PersonRepository personRepository;
    @Mock private JavaMailSender mailSender;

    @InjectMocks
    private BirthdayReminder birthdayReminder;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        injectStaticMailSender(mailSender);
        setField(birthdayReminder, "notificationEmail", "test@example.com");
    }

    @AfterEach
    void tearDown() throws Exception {
        injectStaticMailSender(null);
    }

    private void injectStaticMailSender(JavaMailSender sender) throws Exception {
        Field field = EmailService.class.getDeclaredField("staticMailSender");
        field.setAccessible(true);
        field.set(null, sender);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    /** Returns a date matching today's month and day, using year 2000 (a leap year,
     *  so Feb 29 is always valid). */
    private LocalDate dobMatchingToday() {
        LocalDate today = LocalDate.now();
        return LocalDate.of(2000, today.getMonthValue(), today.getDayOfMonth());
    }

    /** Returns a date whose month-day never matches today: Jan 1 unless today is Jan 1,
     *  in which case Jan 2. */
    private LocalDate dobNotMatchingToday() {
        LocalDate today = LocalDate.now();
        boolean isJan1 = today.getMonthValue() == 1 && today.getDayOfMonth() == 1;
        return isJan1 ? LocalDate.of(1990, 1, 2) : LocalDate.of(1990, 1, 1);
    }

    @Test
    void birthdayMatchesToday_sendsEmailWithCorrectRecipientAndSubject() {
        Person person = new Person();
        person.setName("Alice");
        person.setSurname("Smith");
        person.setDateOfBirth(dobMatchingToday());
        when(personRepository.findAll()).thenReturn(List.of(person));

        birthdayReminder.sendBirthdayReminders();

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertArrayEquals(new String[]{"test@example.com"}, sent.getTo());
        assertTrue(sent.getSubject().contains("Alice"));
        assertTrue(sent.getSubject().contains("Smith"));
    }

    @Test
    void multipleBirthdaysToday_sendsOneEmailPerPerson() {
        Person p1 = new Person();
        p1.setName("Alice"); p1.setSurname("A");
        p1.setDateOfBirth(dobMatchingToday());

        Person p2 = new Person();
        p2.setName("Bob"); p2.setSurname("B");
        p2.setDateOfBirth(LocalDate.of(1990, dobMatchingToday().getMonthValue(), dobMatchingToday().getDayOfMonth()));

        when(personRepository.findAll()).thenReturn(List.of(p1, p2));

        birthdayReminder.sendBirthdayReminders();

        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void noBirthdayToday_noEmailSent() {
        Person person = new Person();
        person.setName("Bob"); person.setSurname("Jones");
        person.setDateOfBirth(dobNotMatchingToday());
        when(personRepository.findAll()).thenReturn(List.of(person));

        birthdayReminder.sendBirthdayReminders();

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void nullDateOfBirth_skippedSilently() {
        Person person = new Person();
        person.setName("Unknown"); person.setSurname("Person");
        person.setDateOfBirth(null);
        when(personRepository.findAll()).thenReturn(List.of(person));

        birthdayReminder.sendBirthdayReminders();

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void placeholderDate_skippedEvenWhenMonthDayMatchesToday() {
        LocalDate today = LocalDate.now();
        Person person = new Person();
        person.setName("Old"); person.setSurname("Person");
        // year <= 1000 is treated as a placeholder regardless of month/day
        person.setDateOfBirth(LocalDate.of(1000, today.getMonthValue(), today.getDayOfMonth()));
        when(personRepository.findAll()).thenReturn(List.of(person));

        birthdayReminder.sendBirthdayReminders();

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void emailSendFailure_doesNotPropagateAndContinuesToNextPerson() {
        Person p1 = new Person();
        p1.setName("Failing"); p1.setSurname("Person");
        p1.setDateOfBirth(dobMatchingToday());

        Person p2 = new Person();
        p2.setName("Success"); p2.setSurname("Person");
        p2.setDateOfBirth(dobMatchingToday());

        when(personRepository.findAll()).thenReturn(List.of(p1, p2));
        doThrow(new RuntimeException("SMTP error"))
                .doNothing()
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> birthdayReminder.sendBirthdayReminders());
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }
}
