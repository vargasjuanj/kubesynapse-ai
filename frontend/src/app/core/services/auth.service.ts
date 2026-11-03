import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export type UserRole = 'ROLE_ADMIN' | 'ROLE_OPERATOR';

export interface UserProfile {
  username: string;
  fullName: string;
  email: string;
  role: UserRole;
  roles: UserRole[];
  description: string;
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private static readonly STORAGE_KEY = 'kubesynapse_user';

  private readonly adminUser: UserProfile = {
    username: 'admin',
    fullName: 'Juan Vargas (SRE Admin)',
    email: 'admin@kubesynapse.io',
    role: 'ROLE_ADMIN',
    roles: ['ROLE_ADMIN', 'ROLE_OPERATOR'],
    description: 'Acceso Total: Mutación del Clúster, Inyección de Caos CKA y Remediación.',
    token: 'dev-admin-jwt-token'
  };

  private readonly operatorUser: UserProfile = {
    username: 'operator',
    fullName: 'Operador CKA (Read-Only)',
    email: 'operator@kubesynapse.io',
    role: 'ROLE_OPERATOR',
    roles: ['ROLE_OPERATOR'],
    description: 'Acceso Seguro: Visualización de Topología, Diagnóstico IA y Verificación.',
    token: 'dev-operator-jwt-token'
  };

  private currentUserSubject: BehaviorSubject<UserProfile>;
  public currentUser$: Observable<UserProfile>;

  constructor() {
    const saved = localStorage.getItem(AuthService.STORAGE_KEY);
    const initial = saved === 'operator' ? this.operatorUser : this.adminUser;
    this.currentUserSubject = new BehaviorSubject<UserProfile>(initial);
    this.currentUser$ = this.currentUserSubject.asObservable();
  }

  public getCurrentUser(): UserProfile {
    return this.currentUserSubject.value;
  }

  public isAdmin(): boolean {
    return this.currentUserSubject.value.role === 'ROLE_ADMIN';
  }

  public switchUser(target: 'admin' | 'operator'): void {
    const user = target === 'admin' ? this.adminUser : this.operatorUser;
    localStorage.setItem(AuthService.STORAGE_KEY, target);
    this.currentUserSubject.next(user);
  }
}
