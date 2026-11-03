import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ClusterOverview, PodStatusInfo, DiagnosticSeverity } from '../../core/models/cluster.model';
import { SeverityBadgeComponent } from '../../shared/components/severity-badge/severity-badge.component';

/**
 * ======================================================================================
 * PATRÓN: SMART / CONTAINER COMPONENT (Dashboard del Clúster)
 * PATRÓN DE DISEÑO: MATERIAL DESIGN M3 (Angular Material)
 * ======================================================================================
 * Rol en la arquitectura:
 * - Renderiza métricas clave en tarjetas ejecutivas de Material (<mat-card>).
 * - Despliega la lista de Pods del clúster utilizando <mat-table> de alto rendimiento.
 * - Emite eventos (@Output) cuando el operador selecciona un pod para diagnóstico IA.
 * ======================================================================================
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatChipsModule,
    MatButtonModule,
    MatIconModule,
    SeverityBadgeComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  @Input() overview: ClusterOverview | null = null;
  @Input() pods: PodStatusInfo[] = [];
  @Output() onSelectPod = new EventEmitter<PodStatusInfo>();

  displayedColumns: string[] = ['name', 'namespace', 'status', 'restarts', 'exitCode', 'actions'];

  getPodSeverity(pod: PodStatusInfo): DiagnosticSeverity {
    if (pod.restartCount >= 5 || pod.phase === 'Failed' || (pod.lastExitCode && pod.lastExitCode !== 0)) {
      return 'CRITICAL';
    }
    if (pod.restartCount > 0 || !pod.ready || pod.phase === 'Pending') {
      return 'WARNING';
    }
    return 'HEALTHY';
  }

  selectPodForDiagnostic(pod: PodStatusInfo): void {
    this.onSelectPod.emit(pod);
  }
}
