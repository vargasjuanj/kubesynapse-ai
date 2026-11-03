package com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.repository;

import com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, String> {

    List<OutboxEventEntity> findTop20ByStatusOrderByCreatedAtAsc(String status);

    long countByStatus(String status);
}
