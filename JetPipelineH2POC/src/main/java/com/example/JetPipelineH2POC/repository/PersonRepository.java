package jet_pipeline_h2_poc.JetPipelineH2POC.src.main.java.com.example.JetPipelineH2POC.repository;

import jet_pipeline_h2_poc.JetPipelineH2POC.src.main.java.com.example.JetPipelineH2POC.person.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}
