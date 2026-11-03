package com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.mongo;

import com.juanvargas.kubesynapse.domain.model.cka.CkaDomain;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataMongoCkaExerciseRepository extends MongoRepository<CkaExerciseDocument, String> {

    List<CkaExerciseDocument> findByDomain(CkaDomain domain);
}
