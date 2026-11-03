package com.juanvargas.kubesynapse.domain.exception;

public class ExerciseNotFoundException extends RuntimeException {
    public ExerciseNotFoundException(String exerciseId) {
        super(String.format("Ejercicio CKA no encontrado: %s", exerciseId));
    }
}
