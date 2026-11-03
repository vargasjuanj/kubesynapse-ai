package com.juanvargas.kubesynapse.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScaleDeploymentRequest {
    @NotBlank(message = "El namespace es obligatorio")
    private String namespace = "default";

    @NotBlank(message = "El nombre del deployment es obligatorio")
    private String deploymentName;

    @Min(value = 0, message = "Las réplicas no pueden ser negativas")
    private int targetReplicas = 1;

    @Min(value = 0, message = "Las réplicas previas no pueden ser negativas")
    private int previousReplicas = 1;

    private boolean dryRun = false;
}
