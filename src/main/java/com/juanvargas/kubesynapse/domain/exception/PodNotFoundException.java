package com.juanvargas.kubesynapse.domain.exception;

public class PodNotFoundException extends RuntimeException {
    public PodNotFoundException(String namespace, String podName) {
        super(String.format("Pod no encontrado en el clúster: %s/%s", namespace, podName));
    }
}
