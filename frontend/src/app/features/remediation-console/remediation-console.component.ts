import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RemediationResult } from '../../core/models/cluster.model';
import { AuthService } from '../../core/services/auth.service';

/**
 * ======================================================================================
 * PATRÓN: SMART COMPONENT (Consola de Operaciones y Remediación - Command Pattern)
 * PATRÓN DE DISEÑO: MATERIAL DESIGN M3 (Angular Material)
 * ======================================================================================
 * Rol en la arquitectura:
 * - Permite ejecutar comandos de remediación operativa de Kubernetes.
 * - Modo Dry-Run (Simulación): Permite a los operadores verificar qué ocurriría
 *   sin afectar el clúster real mediante un switch <mat-slide-toggle>.
 * - Soporte de Undo (Deshacer): Invoca el rollback del último comando en la pila del Invoker.
 * - Tabla de Auditoría: Muestra el log inmutable de comandos ejecutados con <mat-table>.
 * - Control de Acceso RBAC: Bloquea operaciones mutantes si el usuario es ROLE_OPERATOR.
 * ======================================================================================
 */
@Component({
  selector: 'app-remediation-console',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatSlideToggleModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatChipsModule,
    MatTooltipModule
  ],
  templateUrl: './remediation-console.component.html',
  styleUrl: './remediation-console.component.css'
})
export class RemediationConsoleComponent {
  public readonly authService = inject(AuthService);

  @Input() history: RemediationResult[] = [];
  @Input() restartNamespace: string = 'default';
  @Input() restartPodName: string = 'demo-oomkilled-service';
  @Output() onRestart = new EventEmitter<{ namespace: string; podName: string; dryRun: boolean }>();
  @Output() onUndo = new EventEmitter<void>();

  dryRun: boolean = true;

  displayedColumns: string[] = ['commandId', 'action', 'target', 'mode', 'result', 'message', 'time'];

  triggerRestartPod(): void {
    if (this.restartPodName && this.authService.isAdmin()) {
      this.onRestart.emit({
        namespace: this.restartNamespace.trim() || 'default',
        podName: this.restartPodName.trim(),
        dryRun: this.dryRun
      });
    }
  }
}
