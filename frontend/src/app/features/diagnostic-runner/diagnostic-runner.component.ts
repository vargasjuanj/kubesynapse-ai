import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';
import { DiagnosticReport } from '../../core/models/cluster.model';
import { SeverityBadgeComponent } from '../../shared/components/severity-badge/severity-badge.component';

/**
 * ======================================================================================
 * PATRÓN: SMART COMPONENT (Ejecutor y Visor de Diagnóstico con IA)
 * PATRÓN DE DISEÑO: MATERIAL DESIGN M3 (Angular Material)
 * ======================================================================================
 * Rol en la arquitectura:
 * - Permite parametrizar y solicitar el diagnóstico inteligente de un Pod de Minikube.
 * - Renderiza el informe generado por KubeGuardian AI:
 *   1. Severidad y Causa Raíz (Strategy Pattern)
 *   2. Recomendación de IA generada por Ollama / Spring AI
 *   3. Pista de auditoría del pipeline (Chain of Responsibility trail)
 *   4. Acciones de remediación sugeridas
 * ======================================================================================
 */
@Component({
  selector: 'app-diagnostic-runner',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatChipsModule,
    SeverityBadgeComponent
  ],
  templateUrl: './diagnostic-runner.component.html',
  styleUrl: './diagnostic-runner.component.css'
})
export class DiagnosticRunnerComponent {
  @Input() namespace: string = 'kube-system';
  @Input() podName: string = 'storage-provisioner';
  @Input() report: DiagnosticReport | null = null;
  @Input() isLoading: boolean = false;
  @Output() onRunDiagnostic = new EventEmitter<{ namespace: string; podName: string }>();

  submitDiagnostic(): void {
    if (this.podName && this.namespace) {
      this.onRunDiagnostic.emit({
        namespace: this.namespace.trim(),
        podName: this.podName.trim()
      });
    }
  }
}
