package com.juanvargas.kubesynapse.application.service;

import com.juanvargas.kubesynapse.domain.model.ClusterOverview;
import com.juanvargas.kubesynapse.domain.model.PodStatusInfo;
import com.juanvargas.kubesynapse.application.port.in.GetClusterOverviewUseCase;
import com.juanvargas.kubesynapse.application.port.out.KubernetesPort;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain Service: Implements GetClusterOverviewUseCase.
 * Aggregates cluster topology, nodes, namespaces, and pod health.
 */
public class ClusterOverviewService implements GetClusterOverviewUseCase {

    private final KubernetesPort k8sPort;

    public ClusterOverviewService(KubernetesPort k8sPort) {
        this.k8sPort = k8sPort;
    }

    @Override
    public ClusterOverview getClusterOverview() {
        List<String> nodes = k8sPort.listNodeNames();
        List<String> namespaces = k8sPort.listNamespaces();

        List<PodStatusInfo> allPods = new ArrayList<>();
        for (String ns : namespaces) {
            allPods.addAll(k8sPort.listPods(ns));
        }

        int total = allPods.size();
        int healthy = 0;
        List<PodStatusInfo> troubled = new ArrayList<>();

        for (PodStatusInfo pod : allPods) {
            boolean isTroubled = "Failed".equalsIgnoreCase(pod.getPhase())
                    || "Pending".equalsIgnoreCase(pod.getPhase())
                    || pod.getRestartCount() > 2
                    || !pod.isReady();

            if (isTroubled) {
                troubled.add(pod);
            } else {
                healthy++;
            }
        }

        return ClusterOverview.builder()
                .clusterName("local-minikube")
                .nodes(nodes)
                .namespaces(namespaces)
                .totalPods(total)
                .healthyPods(healthy)
                .troubledPods(troubled.size())
                .recentTroubledPods(troubled)
                .build();
    }
}
