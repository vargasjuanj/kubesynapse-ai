import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DiagnosticSeverity } from '../../../core/models/cluster.model';

/**
 * ======================================================================================
 * PATRÓN DE DISEÑO: PRESENTATIONAL / DUMB COMPONENT (Componente de Presentación)
 * ======================================================================================
 * Propósito:
 * - Recibe datos vía @Input() y renderiza la insignia visual con el color
 *   adecuado según la severidad del diagnóstico (HEALTHY, WARNING, CRITICAL).
 * - Separación limpia: HTML en .html, CSS en .css, lógica en .ts.
 * ======================================================================================
 */
@Component({
  selector: 'app-severity-badge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './severity-badge.component.html',
  styleUrl: './severity-badge.component.css'
})
export class SeverityBadgeComponent {
  @Input() severity: DiagnosticSeverity | string = 'HEALTHY';

  getBadgeClass(): string {
    switch (this.severity) {
      case 'CRITICAL': return 'badge-critical';
      case 'WARNING': return 'badge-warning';
      case 'HEALTHY':
      default: return 'badge-healthy';
    }
  }
}
