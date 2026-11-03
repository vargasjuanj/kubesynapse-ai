import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AgentInstructionResponse } from '../../core/models/cluster.model';
import { AuthService } from '../../core/services/auth.service';

/**
 * ======================================================================================
 * PATRÓN: SMART COMPONENT — AI COPILOT INTERACTIVO (Lenguaje Natural & MCP)
 * ======================================================================================
 * Rol en la arquitectura:
 * - Permite al operador humano interactuar en lenguaje natural con KubeGuardian AI.
 * - Despliega los pasos de razonamiento Chain-of-Thought (pensamiento paso a paso).
 * - Identifica la herramienta MCP invocada (Model Context Protocol).
 * - Ofrece "Quick Prompts" ejecutables con un solo clic.
 * - Aplica salvaguardas RBAC visuales si el usuario actual es ROLE_OPERATOR.
 * ======================================================================================
 */
@Component({
  selector: 'app-ai-copilot',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressBarModule,
    MatDividerModule,
    MatTooltipModule
  ],
  templateUrl: './ai-copilot.component.html',
  styleUrl: './ai-copilot.component.css'
})
export class AiCopilotComponent {
  public readonly authService = inject(AuthService);

  @Input() isLoading: boolean = false;
  @Input() lastResponse: AgentInstructionResponse | null = null;

  @Output() onInstruct = new EventEmitter<string>();
  @Output() onNavigateToDiagnostic = new EventEmitter<string>();

  promptInput: string = 'Revisá el pod demo-oomkilled-service en default y decime por qué falló';

  setPrompt(prompt: string): void {
    this.promptInput = prompt;
    this.sendInstruction();
  }

  sendInstruction(): void {
    if (!this.promptInput.trim() || this.isLoading) {
      return;
    }
    this.onInstruct.emit(this.promptInput.trim());
  }

  formatMarkdown(text: string): string {
    if (!text) return '';
    return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/`([^`]+)`/g, '<code class="font-mono" style="background:#0b1120;padding:2px 6px;border-radius:4px;color:#38bdf8;">$1</code>');
  }
}
