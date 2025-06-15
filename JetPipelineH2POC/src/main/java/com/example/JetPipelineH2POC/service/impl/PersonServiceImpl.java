package com.example.JetPipelineH2POC.service.impl;

import com.example.JetPipelineH2POC.person.Person;
import com.example.JetPipelineH2POC.pipeline.PersonPipeline;
import com.example.JetPipelineH2POC.repository.PersonRepository;
import com.example.JetPipelineH2POC.service.PersonService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.pipeline.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PersonServiceImpl implements PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private HazelcastInstance hazelcastInstance;
    
    @Autowired
    private PersonPipeline personPipeline;

    @Override
    public Person savePerson(Person person) {
        return personRepository.save(person);
    }

    @Override 
    public Person update(Long id, Person person) {
        if(personRepository.existsById(id)){
            person.setId(id);
            return personRepository.save(person);
        }
        return null;
    }

    @Override
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    @Override
    public Optional<Person> getPersonById(Long id) {
        return personRepository.findById(id);
    }

    @Override
    public void deletePerson(Long id) {
        personRepository.deleteById(id);
    }

    @Override
    public File readFromIds(List<Long> ids) {
        if(ids == null || ids.isEmpty()) {
            log.error("No IDs provided for CSV export");
            throw new RuntimeException("No IDs provided for CSV export");
        }
        List<Long> validIds = ids.stream()
                .map(Number::longValue)
                .filter(id -> personRepository.findById(id).isPresent())
                .collect(Collectors.toList());
        
        log.info("Valid IDs for export: {}", validIds);

        if(validIds.isEmpty()) {
            log.error("No valid IDs found for CSV export");
            throw new RuntimeException("No valid IDs found for CSV export");
        }

        File tempDir;
        try {
            // Create a temporary directory for the Hazelcast Jet job output
            tempDir = Files.createTempDirectory("temp1").toFile();
        } catch (IOException e) {
            log.error("Failed to create temp directory for CSV export", e);
            throw new RuntimeException("Failed to create temp directory for CSV export", e);
        }

        try{
            log.info("Starting pipeline job...");
            Pipeline p = personPipeline.writeToCsv(validIds, tempDir.getAbsolutePath());
            hazelcastInstance.getJet().newJob(p).join();
            log.info("Pipeine job completed.");
        } catch (Exception e) {
            log.error("Pipeline failed {}:", e.getMessage(), e);
            throw new RuntimeException("Pipeline failed", e);
        }

        File[] files = tempDir.listFiles();
        if(files != null) {
            for (File f: files) {
                log.info("File in temp dir: {}", f.getAbsolutePath());
            }
        }

        if(files != null && files.length > 0) {
            File resultFile = files[0];
            tempDir.deleteOnExit();
            resultFile.deleteOnExit();
            return resultFile;
        } else {
            log.error("No files found in temp directory: {}", tempDir.getAbsolutePath());
            throw new RuntimeException("CSV export failed: No output found in temp directory");
        }
    }
}

