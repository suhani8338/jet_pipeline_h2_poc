package jet_pipeline_h2_poc.JetPipelineH2POC.src.main.java.com.example.JetPipelineH2POC.service;

import jet_pipeline_h2_poc.JetPipelineH2POC.src.main.java.com.example.JetPipelineH2POC.person.Person;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface PersonService {
    Person savePerson(Person person);
    List<Person> getAllPersons();
    Optional<Person> getPersonById(Long id);
    void deletePerson(Long id);
    File readFromIds(List<Long> ids);
    Person update(Long id, Person person);
}