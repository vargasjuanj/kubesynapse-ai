import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, tap, shareReplay, finalize } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  ClusterOverview,
  DiagnosticReport,
  PodStatusInfo,
  RemediationResult,
  DiagnosePodRequest,
  RestartPodRequest,
  AgentInstructionRequest,
  AgentInstructionResponse,
  CkaExercise,
  CkaLabInjectionResult,
  CkaVerificationResult
} from '../models/cluster.model';

/**
 * ======================================================================================
 * PATRÓN DE DISEÑO: SERVICE LAYER / FACADE (Frontend)
 * ======================================================================================
 * Rol en la arquitectura:
 * - Actúa como una fachada (Facade) reactiva para todos los componentes de la interfaz.
 * - Encapsula las llamadas HTTP hacia el BFF (Spring Boot) en http://localhost:8080.
 * - Mantiene un estado reactivo local con RxJS BehaviorSubject para propagar
 *   actualizaciones del cluster en tiempo real sin recargar la página.
 * - Centraliza el manejo de errores HTTP y logging.
 *
 * Referencias educativas:
 * - Angular Services & Dependency Injection: https://angular.dev/guide/di
 * - RxJS Best Practices: https://rxjs.dev/guide/overview
 * ======================================================================================
 */
@Injectable({
  providedIn: 'root'
})
export class KubeSynapseApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;

  // Estado reactivo del cluster
  private readonly overviewSubject = new BehaviorSubject<ClusterOverview | null>(null);
  public readonly clusterOverview$ = this.overviewSubject.asObservable();

  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  public readonly isLoading$ = this.loadingSubject.asObservable();

  /**
   * Obtiene la topología y estado general del cluster Minikube.
   */
  public fetchClusterOverview(): Observable<ClusterOverview> {
    this.loadingSubject.next(true);
    return this.http.get<ClusterOverview>(`${this.baseUrl}/cluster/overview`).pipe(
      tap(data => this.overviewSubject.next(data)),
      catchError(err => this.handleError(err)),
      finalize(() => this.loadingSubject.next(false)),
      shareReplay(1)
    );
  }

  /**
   * Lista todos los namespaces activos en el cluster.
   */
  public listNamespaces(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/cluster/namespaces`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Lista los pods de un namespace específico.
   */
  public listPods(namespace: string = 'default'): Observable<PodStatusInfo[]> {
    return this.http.get<PodStatusInfo[]>(`${this.baseUrl}/cluster/pods`, {
      params: { namespace }
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Ejecuta el pipeline de diagnóstico de IA sobre un Pod específico.
   */
  public diagnosePod(request: DiagnosePodRequest): Observable<DiagnosticReport> {
    this.loadingSubject.next(true);
    return this.http.post<DiagnosticReport>(`${this.baseUrl}/diagnostics/pod`, request).pipe(
      tap(() => this.loadingSubject.next(false)),
      catchError(this.handleError)
    );
  }

  /**
   * Ejecuta el comando de reinicio de Pod (con opción Dry-Run).
   */
  public restartPod(request: RestartPodRequest): Observable<RemediationResult> {
    return this.http.post<RemediationResult>(`${this.baseUrl}/remediations/restart-pod`, request).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Deshace la última acción de remediación ejecutada (Command Undo).
   */
  public undoLastRemediation(): Observable<RemediationResult> {
    return this.http.post<RemediationResult>(`${this.baseUrl}/remediations/undo`, {}).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Obtiene el historial de auditoría de comandos ejecutados.
   */
  public getRemediationHistory(): Observable<RemediationResult[]> {
    return this.http.get<RemediationResult[]>(`${this.baseUrl}/remediations/history`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Envía una instrucción en lenguaje natural al Agente Autónomo Copilot.
   */
  public instructAgent(request: AgentInstructionRequest): Observable<AgentInstructionResponse> {
    this.loadingSubject.next(true);
    return this.http.post<AgentInstructionResponse>(`${this.baseUrl}/agent/instruct`, request).pipe(
      catchError(this.handleError),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  // ======================================================================================
  // Laboratorio de Troubleshooting y Desafíos Kubernetes (Curriculum Septiembre 2026)
  // ======================================================================================

  public getCkaExercises(domain?: string): Observable<CkaExercise[]> {
    let params = new HttpParams();
    if (domain) {
      params = params.set('domain', domain);
    }
    return this.http.get<CkaExercise[]>(`${this.baseUrl}/cka/exercises`, { params }).pipe(
      catchError(this.handleError)
    );
  }

  public getCkaExercise(id: string): Observable<CkaExercise> {
    return this.http.get<CkaExercise>(`${this.baseUrl}/cka/exercises/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  public injectCkaLab(id: string): Observable<CkaLabInjectionResult> {
    this.loadingSubject.next(true);
    return this.http.post<CkaLabInjectionResult>(`${this.baseUrl}/cka/inject/${id}`, {}).pipe(
      catchError(this.handleError),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  public verifyCkaSolution(id: string): Observable<CkaVerificationResult> {
    this.loadingSubject.next(true);
    return this.http.post<CkaVerificationResult>(`${this.baseUrl}/cka/verify/${id}`, {}).pipe(
      catchError(this.handleError),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  public resetCkaLab(id: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/cka/reset/${id}`, {}).pipe(
      catchError(this.handleError)
    );
  }

  public openLocalTerminal(id: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/cka/terminal/${id}`, {}).pipe(
      catchError(this.handleError)
    );
  }

  private readonly handleError = (error: HttpErrorResponse): Observable<never> => {
    this.loadingSubject.next(false);
    let errorMessage = 'Ocurrió un error inesperado al comunicarse con el Backend';
    if (error?.error instanceof ErrorEvent) {
      errorMessage = `Error del cliente: ${error.error.message}`;
    } else if (error?.status) {
      errorMessage = `Error del servidor K8s (Código ${error.status}): ${error.message}`;
    } else if (error?.message) {
      errorMessage = error.message;
    }
    console.error('[KubeCKA-AI-Agent API Error]:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  };
}
