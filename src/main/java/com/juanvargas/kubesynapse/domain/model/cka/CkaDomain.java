package com.juanvargas.kubesynapse.domain.model.cka;

/**
 * ======================================================================================
 * Dominios de Administración Kubernetes (Alineados con CKA / CNCF) (Certified Kubernetes Administrator)
 * Curriculum Oficial CNCF / Linux Foundation (Septiembre 2026 - Kubernetes v1.31/v1.32)
 * ======================================================================================
 * Representa los 5 dominios de conocimiento evaluados en la administración avanzada de clústeres.
 * ======================================================================================
 */
public enum CkaDomain {
    TROUBLESHOOTING("Troubleshooting", "Diagnóstico y Resolución de Problemas", 30),
    CLUSTER_ARCHITECTURE("Cluster Architecture, Installation & Configuration", "Arquitectura del Clúster, Instalación y Configuración", 25),
    SERVICES_AND_NETWORKING("Services & Networking", "Servicios y Redes", 20),
    WORKLOADS_AND_SCHEDULING("Workloads & Scheduling", "Cargas de Trabajo y Programación", 15),
    STORAGE("Storage", "Almacenamiento Persistente", 10);

    private final String nameEn;
    private final String nameEs;
    private final int weightPercentage;

    CkaDomain(String nameEn, String nameEs, int weightPercentage) {
        this.nameEn = nameEn;
        this.nameEs = nameEs;
        this.weightPercentage = weightPercentage;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getNameEs() {
        return nameEs;
    }

    public int getWeightPercentage() {
        return weightPercentage;
    }
}
