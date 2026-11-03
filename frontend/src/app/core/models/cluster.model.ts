/**
 * ======================================================================================
 * MODELOS DE DOMINIO FRONTEND (TypeScript Interfaces)
 * ======================================================================================
 * Rol en Clean Architecture (Frontend):
 * - Representan las entidades y Value Objects inmutables que circulan entre el
 *   BFF (Spring Boot) y los componentes visuales de Angular.
 * - Aseguran type safety en tiempo de compilación y autocompletado en templates.
 *
 * Referencias educativas:
 * - Angular Official Architecture Styleguide: https://angular.dev/style-guide
 * - Clean Architecture in Frontend (Khalil Stemmler): https://khalilstemmler.com/articles/client-side-architecture/
 * ======================================================================================
 */

export type DiagnosticSeverity = 'HEALTHY' | 'LOW' | 'WARNING' | 'CRITICAL';

export interface PodStatusInfo {
  name: string;
  namespace: string;
  nodeName: string;
  phase: string;
  restartCount: number;
  lastExitCode?: number | null;
  lastTerminationReason?: string | null;
  ready: boolean;
  containerImage: string;
  rawLogs: string[];
  clusterEvents: string[];
  memoryLimit?: string | null;
  memoryRequest?: string | null;
}

export interface DiagnosticReport {
  incidentId: string;
  namespace: string;
  podName: string;
  severity: DiagnosticSeverity;
  rootCause: string;
  technicalDetails: string;
  aiRecommendation: string;
  suggestedRemediations: string[];
  pipelineTrail: string[];
  analyzedAt: string;
}

export interface ClusterOverview {
  clusterName: string;
  nodes: string[];
  namespaces: string[];
  totalPods: number;
  healthyPods: number;
  troubledPods: number;
  recentTroubledPods: PodStatusInfo[];
}

export interface RemediationResult {
  commandId: string;
  action: string;
  target: string;
  success: boolean;
  dryRun: boolean;
  message: string;
  executedAt: string;
}

export interface DiagnosePodRequest {
  namespace: string;
  podName: string;
}

export interface RestartPodRequest {
  namespace: string;
  podName: string;
  dryRun: boolean;
}

export interface AgentInstructionRequest {
  prompt: string;
}

export interface AgentInstructionResponse {
  userPrompt: string;
  intent: string;
  toolInvoked: string;
  targetResource: string;
  thoughtProcess: string[];
  agentResponse: string;
  payload?: any;
  suggestedAction?: string;
  timestamp: string;
}

export type CkaDomain =
  | 'TROUBLESHOOTING'
  | 'CLUSTER_ARCHITECTURE'
  | 'SERVICES_AND_NETWORKING'
  | 'WORKLOADS_AND_SCHEDULING'
  | 'STORAGE';

export type CkaDifficulty = 'EASY' | 'MEDIUM' | 'HARD';

export interface CkaExercise {
  id: string;
  domain: CkaDomain;
  difficulty: CkaDifficulty;
  weightPoints: number;
  timeLimitMinutes: number;
  titleEn: string;
  titleEs: string;
  scenarioEn: string;
  scenarioEs: string;
  startingContext: string;
  targetNamespace: string;
  targetResource: string;
  officialSolutionEn: string;
  officialSolutionEs: string;
  yamlSolution: string;
  fastExamTipEn: string;
  fastExamTipEs: string;
  verificationCriteria: string[];
}

export interface CkaAssertionCheck {
  checkName: string;
  passed: boolean;
  expected: string;
  actual: string;
  message: string;
}

export interface CkaVerificationResult {
  exerciseId: string;
  passed: boolean;
  scoreEarned: number;
  maxScore: number;
  statusMessage: string;
  verifiedAt: string;
  checks: CkaAssertionCheck[];
  studentTips: string;
}

export interface CkaLabInjectionResult {
  exerciseId: string;
  status: 'INJECTED' | 'FAILED';
  namespace: string;
  message: string;
  injectedAt: string;
  resourcesCreated: string[];
  startingCommand: string;
}

