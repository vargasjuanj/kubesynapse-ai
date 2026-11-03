package com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.repository;

import com.juanvargas.kubesynapse.infrastructure.adapter.out.persistence.entity.RemediationAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RemediationAuditJpaRepository extends JpaRepository<RemediationAuditEntity, Long> {
    List<RemediationAuditEntity> findTop50ByOrderByExecutedAtDesc();
    List<RemediationAuditEntity> findByTargetOrderByExecutedAtDesc(String target);
}
