package com.juanvargas.kubesynapse.domain.pattern.chain;

import com.juanvargas.kubesynapse.domain.model.DiagnosticSeverity;
import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Context object passed along the Chain of Responsibility handlers.
 * Collects findings, intermediate deductions, and pipeline audit trail.
 */
@Data
@Builder
public class DiagnosticContext {
    private final String namespace;
    private final String podName;
    private PodStatusInfo podInfo;
    
    @Builder.Default
    private DiagnosticSeverity severity = DiagnosticSeverity.HEALTHY;
    
    @Builder.Default
    private String detectedReason = "UNKNOWN";
    
    @Builder.Default
    private List<String> technicalFindings = new ArrayList<>();
    
    @Builder.Default
    private List<String> pipelineTrail = new ArrayList<>();
    
    private boolean conclusive;

    public void addTrail(String handlerName, String observation) {
        pipelineTrail.add(handlerName + " -> " + observation);
    }

    public void addFinding(String finding) {
        technicalFindings.add(finding);
    }
}
