import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../../core/services/auth.service';

/**
 * ======================================================================================
 * CABECERA PRINCIPAL CON AUTENTICACIÓN KEYCLOAK & RBAC
 * ======================================================================================
 * Muestra:
 * 1. Identidad del sistema (KubeGuardian AI).
 * 2. Estado de conexión con el nodo Minikube v1.35.1.
 * 3. Selector interactivo de Usuario y Rol Keycloak (ROLE_ADMIN vs ROLE_OPERATOR).
 * 4. Botón de refresco global del clúster.
 * ======================================================================================
 */
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatTooltipModule
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  @Input() isLoading: boolean = false;
  @Output() onRefresh = new EventEmitter<void>();

  public readonly authService = inject(AuthService);

  public toggleRole(): void {
    const nextRole = this.authService.isAdmin() ? 'operator' : 'admin';
    this.authService.switchUser(nextRole);
  }
}
