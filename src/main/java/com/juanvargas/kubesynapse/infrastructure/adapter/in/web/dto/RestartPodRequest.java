package com.juanvargas.kubesynapse.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RestartPodRequest {
    @NotBlank(message = "El namespace es obligatorio")
    private String namespace = "default";

    @NotBlank(message = "El nombre del pod es obligatorio")
    private String podName;

    private boolean dryRun = false;
}
