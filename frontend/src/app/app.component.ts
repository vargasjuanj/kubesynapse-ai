import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule } from '@angular/material/tabs';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { KubeSynapseApiService } from './core/services/kubesynapse-api.service';
import { AuthService } from './core/services/auth.service';
import { HeaderComponent } from './shared/components/header/header.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { DiagnosticRunnerComponent } from './features/diagnostic-runner/diagnostic-runner.component';
import { RemediationConsoleComponent } from './features/remediation-console/remediation-console.component';
import { AiCopilotComponent } from './features/ai-copilot/ai-copilot.component';
import { CkaSimulatorComponent } from './features/cka-simulator/cka-simulator.component';
import {
  ClusterOverview,
  DiagnosticReport,
  PodStatusInfo,
  RemediationResult,
  AgentInstructionResponse
} from './core/models/cluster.model';

/**
 * ======================================================================================
 * COMPONENTE RAÍZ (AppComponent - Root Container)
 * PATRÓN DE DISEÑO: MATERIAL DESIGN M3 TABS & SERVICE FACADE
 * ======================================================================================
 * Rol en la arquitectura:
 * - Orquesta las vistas maestras de la aplicación mediante <mat-tab-group>:
 *   1. Copilot IA (Instrucciones en Lenguaje Natural con Chain-of-Thought & MCP)
 *   2. Topología y Métricas del Clúster (Dashboard SRE de Observabilidad)
 *   3. Diagnóstico Detallado con IA (Chain of Responsibility & Strategy)
 *   4. Consola de Operaciones Seguras (Command Pattern con Undo & RBAC)
 *   5. Laboratorio de Troubleshooting y Desafíos Kubernetes (September 2026 Curriculum & 2h Timer)
 * - Comunicación desacoplada y reactiva mediante Observables con RxJS.
 * - Separación limpia: HTML en .html, CSS en .css, lógica en .ts.
 * ======================================================================================
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    MatTabsModule,
    MatIconModule,
    MatButtonModule,
    MatSnackBarModule,
    HeaderComponent,
    DashboardComponent,
    DiagnosticRunnerComponent,
    RemediationConsoleComponent,
    AiCopilotComponent,
    CkaSimulatorComponent
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  private readonly apiService = inject(KubeSynapseApiService);
  public readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);

  selectedTabIndex: number = 0;
  isLoading$ = this.apiService.isLoading$;

  overview: ClusterOverview | null = null;
  pods: PodStatusInfo[] = [];
  remediationHistory: RemediationResult[] = [];

  selectedNamespace: string = 'default';
  selectedPodName: string = 'demo-oomkilled-service';
  currentReport: DiagnosticReport | null = null;

  agentResponse: AgentInstructionResponse | null = null;

  ngOnInit(): void {
    this.loadClusterData();
    this.loadHistory();
  }

  loadClusterData(): void {
    this.apiService.fetchClusterOverview().subscribe({
      next: (data) => {
        this.overview = data;
        this.apiService.listPods('default').subscribe(p => this.pods = p);
      },
      error: (err) => this.showToast('Error al conectar con el clúster: ' + err.message, 'error')
    });
  }

  loadHistory(): void {
    this.apiService.getRemediationHistory().subscribe({
      next: (hist) => this.remediationHistory = hist,
      error: (err) => console.error('Error al cargar historial:', err)
    });
  }

  handleAgentInstruction(prompt: string): void {
    this.apiService.instructAgent({ prompt }).subscribe({
      next: (resp) => {
        this.agentResponse = resp;
        this.showToast(`Agente: instrucción procesada exitosamente.`, 'info');
        if (resp.payload && resp.intent === 'DIAGNOSE_POD') {
          this.currentReport = resp.payload as DiagnosticReport;
        }
      },
      error: (err) => this.showToast('Falla del Agente: ' + err.message, 'error')
    });
  }

  navigateToDiagnosticTab(targetResource: string): void {
    if (targetResource && targetResource.includes('/')) {
      const parts = targetResource.split('/');
      this.selectedNamespace = parts[0];
      this.selectedPodName = parts[1];
    }
    this.currentReport = null; // Limpiar reporte anterior
    this.selectedTabIndex = 2; // Pestaña de Diagnosticar y Reparar
  }

  onPodSelectedFromDashboard(pod: PodStatusInfo): void {
    this.selectedNamespace = pod.namespace;
    this.selectedPodName = pod.name;
    this.currentReport = null; // Limpiar reporte anterior
    this.selectedTabIndex = 2; // Cambiar a la pestaña de Diagnosticar y Reparar
    this.runDiagnostic({ namespace: pod.namespace, podName: pod.name });
  }

  runDiagnostic(event: { namespace: string; podName: string }): void {
    this.selectedNamespace = event.namespace;
    this.selectedPodName = event.podName;
    this.currentReport = null; // Limpiar reporte anterior de inmediato para evitar datos residuales
    this.apiService.diagnosePod(event).subscribe({
      next: (report) => {
        this.currentReport = report;
        this.showToast(`Diagnóstico completado para ${report.podName} (${report.severity})`, 'info');
      },
      error: (err) => this.showToast('Falla en diagnóstico: ' + err.message, 'error')
    });
  }

  handleRestart(event: { namespace: string; podName: string; dryRun: boolean }): void {
    this.apiService.restartPod(event).subscribe({
      next: (res) => {
        this.loadHistory();
        this.showToast(res.message, res.success ? 'success' : 'warn');
      },
      error: (err) => this.showToast('Falla al reiniciar: ' + err.message, 'error')
    });
  }

  handleUndo(): void {
    this.apiService.undoLastRemediation().subscribe({
      next: (res) => {
        this.loadHistory();
        this.showToast(res.message, 'info');
      },
      error: (err) => this.showToast('Falla al deshacer: ' + err.message, 'error')
    });
  }

  switchToAdmin(): void {
    this.authService.switchUser('admin');
    this.showToast('Perfil cambiado a SRE Admin (Permisos totales habilitados).', 'info');
  }

  private showToast(message: string, type: 'info' | 'success' | 'warn' | 'error'): void {
    this.snackBar.open(message, 'Cerrar', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'bottom'
    });
  }
}
