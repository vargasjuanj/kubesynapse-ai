package com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.mapper;

import com.juanvargas.kubesynapse.domain.model.cka.CkaExercise;
import com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.mongo.CkaExerciseDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * ======================================================================================
 * MAPPER MAPSTRUCT: DOMINIO <-> MONGODB DOCUMENT
 * ======================================================================================
 * Aplica el patrón de Gabriel Chaldú con MapStruct para transformar limpiamente
 * modelos de dominio puros en documentos de persistencia NoSQL de MongoDB.
 * ======================================================================================
 */
@Mapper(componentModel = "spring")
public interface CkaExercisePersistenceMapper {

    CkaExercise toDomain(CkaExerciseDocument document);

    @Mapping(target = "dynamicGenerated", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CkaExerciseDocument toDocument(CkaExercise domain);

    List<CkaExercise> toDomainList(List<CkaExerciseDocument> documents);

    List<CkaExerciseDocument> toDocumentList(List<CkaExercise> domainList);
}
