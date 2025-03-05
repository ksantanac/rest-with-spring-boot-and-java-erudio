package br.com.erudio;

import br.com.erudio.model.Person;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@Service
public class PersonServices {

    private final AtomicLong counter = new AtomicLong();

    private Logger logger = Logger.getLogger(PersonServices.class.getName());

    public Person findById(String id) {
        logger.info("Finding one Person!");

        Person person = new Person();
        person.setId(counter.incrementAndGet());
        person.setFirstName("Kaue");
        person.setLastName("Santana");
        person.setAddress("Santana de Parnaiba - SP - Brasil");
        person.setGender("Male");

        return person;
    }
}
