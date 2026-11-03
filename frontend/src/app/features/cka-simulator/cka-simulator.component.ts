import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { KubeSynapseApiService } from '../../core/services/kubesynapse-api.service';
import { AuthService } from '../../core/services/auth.service';
import {
  CkaExercise,
  CkaDomain,
  CkaVerificationResult,
  CkaLabInjectionResult
} from '../../core/models/cluster.model';

/**
 * ======================================================================================
 * Laboratorio de Troubleshooting y Desafíos Kubernetes (Curriculum Septiembre 2026 - Kubernetes v1.31 / v1.32)
 * ======================================================================================
 * Provee:
 * 1. Temporizador de Resolución de Incidentes (Time-Boxed SLA: 120 min).
 * 2. Catálogo bilingüe de ejercicios oficiales por los 5 dominios CNCF.
 * 3. Tarjeta instructiva para resolver en terminal local (CMD / PowerShell / Bash).
 * 4. Inyección idempotente de escenarios reales en Minikube (Pods, PVCs, NetPol, RBAC).
 * 5. Evaluación automatizada con aserciones en vivo sobre el clúster.
 * 6. Control RBAC: ROLE_OPERATOR sólo lectura vs ROLE_ADMIN.
 * ======================================================================================
 */
@Component({
  selector: 'app-cka-simulator',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatBadgeModule,
    MatProgressBarModule,
    MatTooltipModule,
    MatExpansionModule,
    MatSnackBarModule
  ],
  templateUrl: './cka-simulator.component.html',
  styleUrl: './cka-simulator.component.css'
})
export class CkaSimulatorComponent implements OnInit, OnDestroy {
  private readonly apiService = inject(KubeSynapseApiService);
  public readonly authService = inject(AuthService);
  private readonly snackBar = inject(MatSnackBar);

  public allExercises: CkaExercise[] = [];
  public filteredExercises: CkaExercise[] = [];
  public selectedExercise: CkaExercise | null = null;
  public selectedDomain: string = 'ALL';
  public currentLang: 'EN' | 'ES' = 'ES';

  public isInjecting: boolean = false;
  public isVerifying: boolean = false;
  public injectionResult: CkaLabInjectionResult | null = null;
  public verificationResult: CkaVerificationResult | null = null;

  // Temporizador Oficial CKA (120 minutos = 7200 segundos)
  public readonly TOTAL_EXAM_SECONDS: number = 7200;
  public remainingSeconds: number = 7200;
  public isTimerRunning: boolean = false;
  private timerInterval: any = null;

  ngOnInit(): void {
    this.loadExercises();
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }

  public loadExercises(): void {
    this.apiService.getCkaExercises().subscribe({
      next: (exercises) => {
        this.allExercises = exercises;
        this.filterByDomain(this.selectedDomain);
        if (this.filteredExercises.length > 0 && !this.selectedExercise) {
          this.selectExercise(this.filteredExercises[0]);
        }
      },
      error: (err) => {
        this.snackBar.open('Error cargando catálogo CKA: ' + err.message, 'Cerrar', { duration: 4000 });
      }
    });
  }

  public filterByDomain(domain: string): void {
    this.selectedDomain = domain;
    if (domain === 'ALL') {
      this.filteredExercises = [...this.allExercises];
    } else {
      this.filteredExercises = this.allExercises.filter(e => e.domain === domain);
    }
    if (this.filteredExercises.length > 0) {
      this.selectExercise(this.filteredExercises[0]);
    }
  }

  public selectExercise(ex: CkaExercise): void {
    this.selectedExercise = ex;
    this.injectionResult = null;
    this.verificationResult = null;
  }

  public injectLab(): void {
    if (!this.selectedExercise || this.isInjecting || !this.authService.isAdmin()) return;
    this.isInjecting = true;
    this.injectionResult = null;
    this.verificationResult = null; // Limpiar resultado previo para evitar confusiones

    this.apiService.injectCkaLab(this.selectedExercise.id).subscribe({
      next: (result) => {
        this.isInjecting = false;
        this.injectionResult = result;
        this.snackBar.open(result.message, 'Entendido', { duration: 5000 });
      },
      error: (err) => {
        this.isInjecting = false;
        this.snackBar.open('Error al inyectar laboratorio: ' + err.message, 'Cerrar', { duration: 5000 });
      }
    });
  }

  public verifySolution(): void {
    if (!this.selectedExercise || this.isVerifying) return;
    this.isVerifying = true;
    this.verificationResult = null;

    this.apiService.verifyCkaSolution(this.selectedExercise.id).subscribe({
      next: (result) => {
        this.isVerifying = false;
        this.verificationResult = result;
        const msg = result.passed ? '🎉 ¡Aprobado!' : '⚠️ Revisa los requisitos pendientes';
        this.snackBar.open(msg, 'Ver desglose', { duration: 4000 });
      },
      error: (err) => {
        this.isVerifying = false;
        this.snackBar.open('Error evaluando solución: ' + err.message, 'Cerrar', { duration: 5000 });
      }
    });
  }

  public resetLab(): void {
    if (!this.selectedExercise || !this.authService.isAdmin()) return;
    this.apiService.resetCkaLab(this.selectedExercise.id).subscribe({
      next: () => {
        this.injectionResult = null;
        this.verificationResult = null;
        this.snackBar.open('🧹 Laboratorio reiniciado y namespace limpiado.', 'OK', { duration: 3000 });
      },
      error: (err) => {
        this.snackBar.open('Error al limpiar laboratorio: ' + err.message, 'Cerrar', { duration: 4000 });
      }
    });
  }

  // --- TEMPORIZADOR PSI 2 HORAS ---
  public toggleTimer(): void {
    if (this.isTimerRunning) {
      this.stopTimer();
    } else {
      this.startTimer();
    }
  }

  public startTimer(): void {
    if (this.timerInterval) return;
    this.isTimerRunning = true;
    this.timerInterval = setInterval(() => {
      if (this.remainingSeconds > 0) {
        this.remainingSeconds--;
      } else {
        this.stopTimer();
        this.snackBar.open('⏰ ¡Tiempo límite de resolución finalizado! (2 horas)', 'Cerrar', { duration: 8000 });
      }
    }, 1000);
  }

  public stopTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
    this.isTimerRunning = false;
  }

  public resetTimer(): void {
    this.stopTimer();
    this.remainingSeconds = this.TOTAL_EXAM_SECONDS;
  }

  public formatTime(totalSec: number): string {
    const hours = Math.floor(totalSec / 3600);
    const minutes = Math.floor((totalSec % 3600) / 60);
    const seconds = totalSec % 60;
    const h = hours < 10 ? '0' + hours : '' + hours;
    const m = minutes < 10 ? '0' + minutes : '' + minutes;
    const s = seconds < 10 ? '0' + seconds : '' + seconds;
    return `${h}:${m}:${s}`;
  }

  public getTimerUrgencyClass(): string {
    if (this.remainingSeconds <= 300) { // < 5 min
      return 'timer-danger';
    }
    if (this.remainingSeconds <= 900) { // < 15 min
      return 'timer-warning';
    }
    return 'timer-normal';
  }

  // --- COPIADO AL PORTAPAPELES ---
  public copyToClipboard(command: string): void {
    if (navigator?.clipboard) {
      navigator.clipboard.writeText(command).then(() => {
        this.snackBar.open('📋 Comando copiado al portapapeles: ' + command, 'Listo', { duration: 2500 });
      }).catch(() => {
        this.fallbackCopy(command);
      });
    } else {
      this.fallbackCopy(command);
    }
  }

  private fallbackCopy(text: string): void {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.select();
    try {
      document.execCommand('copy');
      this.snackBar.open('📋 Comando copiado: ' + text, 'Listo', { duration: 2500 });
    } catch {
      this.snackBar.open('Selecciona el comando manualmente', 'Cerrar', { duration: 3000 });
    }
    document.body.removeChild(textArea);
  }

  public openTerminal(): void {
    if (!this.selectedExercise) return;
    this.apiService.openLocalTerminal(this.selectedExercise.id).subscribe({
      next: () => {
        this.snackBar.open('💻 Ventana de PowerShell abierta en tu escritorio con contexto CKA', 'OK', { duration: 3500 });
      },
      error: () => {
        this.snackBar.open('No se pudo abrir la terminal automáticamente. Copia el comando arriba.', 'OK', { duration: 3000 });
      }
    });
  }

  public downloadPdf(): void {
    if (!this.selectedExercise) return;
    const url = `http://localhost:8080/api/v1/cka/exercises/${this.selectedExercise.id}/pdf?studentName=${encodeURIComponent(this.authService.getCurrentUser().fullName)}`;
    window.open(url, '_blank');
  }
}
