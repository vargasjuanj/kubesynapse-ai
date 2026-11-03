package com.juanvargas.kubesynapse.domain.model.cka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detalle individual de una aserción técnica evaluada en el clúster.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CkaAssertionCheck {
    private String checkName;
    private boolean passed;
    private String expected;
    private String actual;
    private String message;
}
