package com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.mongo;

import com.juanvargas.kubesynapse.application.port.out.CkaExerciseRepositoryPort;
import com.juanvargas.kubesynapse.domain.model.cka.CkaDomain;
import com.juanvargas.kubesynapse.domain.model.cka.CkaExercise;
import com.juanvargas.kubesynapse.domain.model.cka.CkaExerciseCatalog;
import com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.mapper.CkaExercisePersistenceMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * ======================================================================================
 * ADAPTADOR DE PERSISTENCIA MONGODB (Driven Adapter): REPOSITORIO DINÁMICO CKA
 * ======================================================================================
 * Implementa {@link CkaExerciseRepositoryPort}.
 * Gestiona el almacenamiento NoSQL de preguntas y laboratorios CKA en MongoDB.
 * Auto-inicializa el catálogo inicial y permite agregar preguntas dinámicas ilimitadas.
 * ======================================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MongoCkaExerciseRepositoryAdapter implements CkaExerciseRepositoryPort {

    private final SpringDataMongoCkaExerciseRepository mongoRepo;
    private final CkaExercisePersistenceMapper mapper;

    @PostConstruct
    public void seedInitialCatalogIfEmpty() {
        try {
            long count = mongoRepo.count();
            if (count == 0) {
                log.info("🍃 [MongoDB Seeder]: Colección 'cka_exercises' vacía. Cargando catálogo inicial CKA 2026...");
                List<CkaExercise> initial = CkaExerciseCatalog.getAllExercises();
                List<CkaExerciseDocument> docs = mapper.toDocumentList(initial);
                mongoRepo.saveAll(docs);
                log.info("✅ [MongoDB Seeder]: {} ejercicios iniciales persistidos exitosamente en MongoDB.", initial.size());
            } else {
                log.info("🍃 [MongoDB]: Colección 'cka_exercises' activa con {} ejercicios registrados.", count);
            }
        } catch (Exception ex) {
            log.warn("⚠️ [MongoDB Fallback]: No se pudo conectar a MongoDB ({}); operando con catálogo en memoria.", ex.getMessage());
        }
    }

    @Override
    public List<CkaExercise> findAll() {
        try {
            List<CkaExerciseDocument> docs = mongoRepo.findAll();
            if (!docs.isEmpty()) {
                return mapper.toDomainList(docs);
            }
        } catch (Exception ex) {
            log.warn("Aviso: Fallback a memoria al consultar MongoDB: {}", ex.getMessage());
        }
        return CkaExerciseCatalog.getAllExercises();
    }

    @Override
    public List<CkaExercise> findByDomain(CkaDomain domain) {
        try {
            List<CkaExerciseDocument> docs = mongoRepo.findByDomain(domain);
            if (!docs.isEmpty()) {
                return mapper.toDomainList(docs);
            }
        } catch (Exception ex) {
            log.warn("Aviso: Fallback a memoria por dominio en MongoDB: {}", ex.getMessage());
        }
        return CkaExerciseCatalog.getAllExercises().stream()
                .filter(e -> e.getDomain() == domain)
                .toList();
    }

    @Override
    public Optional<CkaExercise> findById(String id) {
        try {
            Optional<CkaExerciseDocument> doc = mongoRepo.findById(id);
            if (doc.isPresent()) {
                return doc.map(mapper::toDomain);
            }
        } catch (Exception ex) {
            log.warn("Aviso: Fallback a memoria por ID en MongoDB: {}", ex.getMessage());
        }
        return CkaExerciseCatalog.getAllExercises().stream()
                .filter(e -> e.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    @Override
    public CkaExercise save(CkaExercise exercise) {
        try {
            CkaExerciseDocument doc = mapper.toDocument(exercise);
            CkaExerciseDocument saved = mongoRepo.save(doc);
            log.info("🍃 [MongoDB]: Ejercicio dinámico '{}' guardado en colección 'cka_exercises'.", exercise.getId());
            return mapper.toDomain(saved);
        } catch (Exception ex) {
            log.warn("No se pudo guardar en MongoDB: {}", ex.getMessage());
            return exercise;
        }
    }

    @Override
    public void saveAll(List<CkaExercise> exercises) {
        try {
            List<CkaExerciseDocument> docs = mapper.toDocumentList(exercises);
            mongoRepo.saveAll(docs);
        } catch (Exception ex) {
            log.warn("No se pudo hacer saveAll en MongoDB: {}", ex.getMessage());
        }
    }

    @Override
    public long count() {
        try {
            return mongoRepo.count();
        } catch (Exception ex) {
            return CkaExerciseCatalog.getAllExercises().size();
        }
    }
}
