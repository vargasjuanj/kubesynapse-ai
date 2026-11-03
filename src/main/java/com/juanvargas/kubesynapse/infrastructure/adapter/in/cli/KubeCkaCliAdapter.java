package com.juanvargas.kubesynapse.infrastructure.adapter.in.cli;

import com.juanvargas.kubesynapse.domain.model.DiagnosticReport;
import com.juanvargas.kubesynapse.application.port.in.DiagnosePodUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * ======================================================================================
 * PATRÓN ARQUITECTÓNICO: INBOUND ADAPTER (Driving / Primary Adapter - Terminal CLI)
 * DEMOSTRACIÓN CLAVE DE ARQUITECTURA HEXAGONAL: MULTI-DRIVER SYSTEM
 * ======================================================================================
 * Rol en la arquitectura:
 * - Demuestra la independencia total del Dominio: el mismo caso de uso ({@link DiagnosePodUseCase})
 *   que atiende la interfaz web Angular (vía REST en DiagnosticController), puede ser invocado
 *   directamente desde una terminal Linux / CKA por un operador SRE mediante flags de consola.
 * - Sin necesidad de levantar navegadores ni frameworks web.
 * - Ideal para administración de servidores y terminales SRE donde todo se opera vía CLI y scripts Bash.
 *
 * Ejemplo de uso en terminal:
 *   java -jar app.jar --cli.pod=storage-provisioner --cli.namespace=kube-system
 *
 * Referencias educativas:
 * - Alistair Cockburn (Hexagonal Architecture Drivers): https://alistair.cockburn.us/hexagonal-architecture/
 * - Spring Boot ApplicationRunner: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.spring-application.application-runner
 * ======================================================================================
 */
@Slf4j
@Component
public class KubeCkaCliAdapter implements ApplicationRunner {

    private final DiagnosePodUseCase diagnoseUseCase;

    public KubeCkaCliAdapter(DiagnosePodUseCase diagnoseUseCase) {
        this.diagnoseUseCase = diagnoseUseCase;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (args.containsOption("cli.pod")) {
            String podName = args.getOptionValues("cli.pod").get(0);
            String namespace = args.containsOption("cli.namespace") 
                    ? args.getOptionValues("cli.namespace").get(0) 
                    : "default";

            log.info("=================================================================");
            log.info("🖥️ [KubeCKA-Synapse CLI Driver]: Ejecutando diagnóstico en terminal...");
            log.info("Objetivo: Namespace='{}' | Pod='{}'", namespace, podName);
            log.info("=================================================================");

            DiagnosticReport report = diagnoseUseCase.diagnosePod(namespace, podName);

            System.out.println("\n------------------------------------------------------------");
            System.out.println(" 🧠 INFORME DE DIAGNÓSTICO KUBECKA AI (TERMINAL MODE)");
            System.out.println("------------------------------------------------------------");
            System.out.println(" ID Incidente : " + report.getIncidentId());
            System.out.println(" Pod Objetivo : " + report.getPodName() + " (" + report.getNamespace() + ")");
            System.out.println(" Severidad    : [" + report.getSeverity() + "]");
            System.out.println(" Causa Raíz   : " + report.getRootCause());
            System.out.println("\n [Consejo SRE / IA]:");
            System.out.println(" " + report.getAiRecommendation());
            System.out.println("\n [Plan de Remediación]:");
            report.getSuggestedRemediations().forEach(rem -> System.out.println("  • " + rem));
            System.out.println("------------------------------------------------------------\n");
        } else {
            log.info("🖥️ [KubeCKA-Synapse CLI Driver]: Adaptador de terminal activo y listo para flags (--cli.pod=<nombre>).");
        }
    }
}
