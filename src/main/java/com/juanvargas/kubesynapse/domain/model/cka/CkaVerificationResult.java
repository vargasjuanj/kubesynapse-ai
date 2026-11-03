package com.juanvargas.kubesynapse.domain.model.cka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Resultado completo de la verificación de un ejercicio CKA contra el clúster real.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CkaVerificationResult {
    private String exerciseId;
    private boolean passed;
    private int scoreEarned;
    private int maxScore;
    private String statusMessage;
    private Instant verifiedAt;
    private List<CkaAssertionCheck> checks;
    private String studentTips;
}
