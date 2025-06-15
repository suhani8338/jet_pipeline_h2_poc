package com.example.JetPipelineH2POC.repository;

import com.example.JetPipelineH2POC.person.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}
