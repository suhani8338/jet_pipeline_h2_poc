package com.example.JetPipelineH2POC.pipeline;
import com.example.JetPipelineH2POC.repository.PersonRepository;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.ServiceFactories;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.Sources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import com.hazelcast.core.HazelcastInstance;

import java.util.List;
import java.util.Objects;
import java.io.Serializable;

@Component
public class PersonPipeline implements Serializable, ApplicationContextAware {

    private static transient ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    @Autowired
    private HazelcastInstance hazelcastInstance;

    /**
     * Builds a Hazelcast Jet pipeline to export Person data to a CSV file.
     * The pipeline reads a list of person IDs, fetches the full Person object
     * from the database for each ID, converts it to a CSV string, and writes
     * it to a file in the specified directory.
     *
     * @param ids The list of Person IDs to export.
     * @param outputDirectory The directory where the output file(s) will be created.
     * @return A configured Hazelcast Jet Pipeline.
     */

    public Pipeline writeToCsv(List<Long> ids, String outputDirectory) {
        hazelcastInstance.getList("source").clear();
        hazelcastInstance.getList("source").addAll(ids);


        Pipeline p = Pipeline.create();

        p.readFrom(Sources.list("source"))
                .mapUsingService(
                        ServiceFactories.sharedService(ctx -> context.getBean(PersonRepository.class)),
                        (PersonRepository repo, Object id) -> repo.findById((Long) id).orElse(null)
                )
                .filter(Objects::nonNull)
                .map(person -> person.getId() + "," + person.getName() + "," + person.getAge())
                .writeTo(Sinks.files(outputDirectory));

        return p;
    }
}

