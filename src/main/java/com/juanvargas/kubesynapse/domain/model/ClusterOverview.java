package com.juanvargas.kubesynapse.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * High-level health and topology overview of the connected Kubernetes cluster.
 */
@Value
@Builder
public class ClusterOverview {
    String clusterName;
    List<String> nodes;
    List<String> namespaces;
    int totalPods;
    int healthyPods;
    int troubledPods;
    List<PodStatusInfo> recentTroubledPods;
}
