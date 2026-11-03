package com.juanvargas.kubesynapse.infrastructure.adapter.in.web;

import com.juanvargas.kubesynapse.domain.exception.ExerciseNotFoundException;
import com.juanvargas.kubesynapse.domain.exception.PodNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * ======================================================================================
 * ADAPTADOR PRIMARIO: MANEJADOR GLOBAL DE EXCEPCIONES (RFC 7807 Problem Details)
 * ======================================================================================
 * Sigue el patrón canónico de Gabriel Chaldú en Spring Boot 3:
 * - Captura excepciones de Dominio (PodNotFoundException, ExerciseNotFoundException)
 * - Mapea a ProblemDetail con HTTP status coherente (404, 400, 409, 500)
 * - Proporciona timestamps y URIs de tipo de error estandarizadas
 * ======================================================================================
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PodNotFoundException.class)
    public ProblemDetail handlePodNotFound(PodNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage()
        );
        problemDetail.setTitle("Pod no encontrado");
        problemDetail.setType(URI.create("urn:problem:pod-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(ExerciseNotFoundException.class)
    public ProblemDetail handleExerciseNotFound(ExerciseNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage()
        );
        problemDetail.setTitle("Ejercicio CKA no encontrado");
        problemDetail.setType(URI.create("urn:problem:exercise-not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Error de validación en los parámetros enviados"
        );
        problemDetail.setTitle("Datos inválidos");
        problemDetail.setType(URI.create("urn:problem:validation-error"));
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, ex.getMessage()
        );
        problemDetail.setTitle("Operación inválida");
        problemDetail.setType(URI.create("urn:problem:invalid-operation"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage() != null ? ex.getMessage() : "Error interno no especificado"
        );
        problemDetail.setTitle("Error interno del servidor");
        problemDetail.setType(URI.create("urn:problem:internal-server-error"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
