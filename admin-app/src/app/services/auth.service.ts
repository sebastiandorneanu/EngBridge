import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { jwtDecode } from 'jwt-decode';

export interface LoginResponse {
  token: string;
  error?: string;
}

export interface JWTPayload {
  sub: string;
  role: string;
  exp: number;
  iss: string;
  jti: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private platformId = inject(PLATFORM_ID);

  constructor(private http: HttpClient) { }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { username, password })
      .pipe(
        tap(response => {
          if (response.error) {
            throw { status: 401, message: response.error };
          }

          const decoded = jwtDecode<JWTPayload>(response.token);

          if (decoded.role !== 'ADMIN') {
            throw { status: 403, message: 'Access denied. Only administrators can use this application.' };
          }

          if (isPlatformBrowser(this.platformId)) {
            localStorage.setItem('jwtToken', response.token);
            localStorage.setItem('currentUser', JSON.stringify({
              uid: decoded.sub,
              role: decoded.role
            }));
          }
        }),
        catchError((error: any) => {
          let errorMessage = 'Login failed.';

          if (error.status === 403) {
            errorMessage = error.message;
          } else if (error.status === 401 || error.error?.status === 401) {
            errorMessage = 'Invalid username or password.';
          } else if (error.status === 0) {
            errorMessage = 'Cannot connect to server.';
          }

          return throwError(() => new Error(errorMessage));
        })
      );
  }
  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('jwtToken');
    }
  }

  isLoggedIn(): boolean {
    if (isPlatformBrowser(this.platformId)) {
      return !!localStorage.getItem('jwtToken');
    }
    return false;
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('jwtToken');
    }
    return null;
  }

  getCurrentUser(): any {
    if (isPlatformBrowser(this.platformId)) {
      const user = localStorage.getItem('currentUser');
      return (user && user !== 'undefined') ? JSON.parse(user) : null;
    }
    return null;
  }

  isAdmin(): boolean {
    const user = this.getCurrentUser();
    return user && user.role === 'ADMIN';
  }

  getUserRole(): string | null {
    const user = this.getCurrentUser();
    return user ? user.role : null;
  }
}
