package com.web.Lixiarchos;

import com.web.Lixiarchos.enums.Language;
import com.web.Lixiarchos.enums.Religion;
import com.web.Lixiarchos.enums.Sex;
import com.web.Lixiarchos.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@EnableScheduling
@SpringBootApplication
@RestController
public class LixiarchosApplication {
	private static final Logger log = LoggerFactory.getLogger(LixiarchosApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(LixiarchosApplication.class, args);
	}

	/*
	@Bean
	CommandLineRunner runner(){

		return args -> {
			Language[] languages = {Language.ENGLISH};
			Date dateOfBirth =  new GregorianCalendar(1994, Calendar.FEBRUARY, 11).getTime();
			Person person = new Person(1, "John", "Doe", Sex.MALE, "Software Engineer", "123 Main Street, New York, USA", dateOfBirth, Religion.CHRISTIAN_ORTHODOX, false, "American", languages, "test@test.com");
			log.info(person.toString());
		};
	}
	*/


}
