package com.juanvargas.kubesynapse.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Domain snapshot of a Kubernetes Pod status and runtime state.
 * Completely decoupled from any external Kubernetes SDK or framework.
 */
@Value
@Builder
public class PodStatusInfo {
    String name;
    String namespace;
    String nodeName;
    String phase; // Running, Pending, Failed, Succeeded, Unknown
    int restartCount;
    Integer lastExitCode;
    String lastTerminationReason; // OOMKilled, Error, Completed, etc.
    boolean ready;
    String containerImage;
    List<String> rawLogs;
    List<String> clusterEvents;
    String memoryLimit;
    String memoryRequest;
}
