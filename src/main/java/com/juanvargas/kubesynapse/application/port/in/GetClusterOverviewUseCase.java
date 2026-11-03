package com.juanvargas.kubesynapse.application.port.in;

import com.juanvargas.kubesynapse.domain.model.ClusterOverview;

/**
 * Inbound Port / Use Case: Retrieves topology and health overview of the cluster.
 */
public interface GetClusterOverviewUseCase {
    ClusterOverview getClusterOverview();
}
