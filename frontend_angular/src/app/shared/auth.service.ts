import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private loggedIn = new BehaviorSubject<boolean>(false);
    private username = new BehaviorSubject<string>('');
    private email = new BehaviorSubject<string>('');
    private userId = new BehaviorSubject<number | null>(null);
    private currentLevel = new BehaviorSubject<number>(1);
    private placementScore = new BehaviorSubject<number | null>(null);
    private completedLessons = new BehaviorSubject<number>(0);
    private totalLessons = new BehaviorSubject<number>(0);

    isLoggedIn$ = this.loggedIn.asObservable();
    username$ = this.username.asObservable();
    email$ = this.email.asObservable();
    userId$ = this.userId.asObservable();
    currentLevel$ = this.currentLevel.asObservable();
    placementScore$ = this.placementScore.asObservable();
    completedLessons$ = this.completedLessons.asObservable();
    totalLessons$ = this.totalLessons.asObservable();

    constructor(
      private router: Router,
      private http: HttpClient,
      @Inject(PLATFORM_ID) private platformId: Object
    ) {
      if (this.isBrowser()) {
        const hasToken = this.hasToken();
        this.loggedIn.next(hasToken);
        this.username.next(this.getStoredUsername());
        this.email.next(localStorage.getItem('email') || '');

        const storedId = localStorage.getItem('userId');
        if (storedId) this.userId.next(parseInt(storedId, 10));

        const storedLevel = localStorage.getItem('currentLevel');

      if (storedLevel) this.currentLevel.next(parseInt(storedLevel, 10));

      const storedScore = localStorage.getItem('placementScore');
      if (storedScore) this.placementScore.next(parseInt(storedScore, 10));

      const storedCompleted = localStorage.getItem('completedLessons');
      if (storedCompleted) this.completedLessons.next(parseInt(storedCompleted, 10));

      const storedTotal = localStorage.getItem('totalLessons');
      if (storedTotal) this.totalLessons.next(parseInt(storedTotal, 10));

      if (hasToken) {
        this.fetchProfile();
        this.fetchUserInfo(this.getStoredUsername());
      }
    }
  }

  fetchUserInfo(username: string) {
    if (!username || username === 'User') return;
    this.http.get<any>(`http://localhost:8081/users/info?username=${username}`).subscribe({
      next: (data) => {
        this.currentLevel.next(data.levelId);
        this.placementScore.next(data.placementTestScore);
        if (data.completedLessons !== undefined) this.completedLessons.next(data.completedLessons);
        if (data.totalLessons !== undefined) this.totalLessons.next(data.totalLessons);

        if (this.isBrowser()) {
          localStorage.setItem('currentLevel', data.levelId.toString());
          if (data.placementTestScore !== null) {
            localStorage.setItem('placementScore', data.placementTestScore.toString());
          }
          localStorage.setItem('completedLessons', (data.completedLessons || 0).toString());
          localStorage.setItem('totalLessons', (data.totalLessons || 0).toString());
        }
      },
      error: (err) => console.error("Failed to fetch user info", err)
    });
  }

  updateUserInfo(levelId: number, score: number) {
    this.currentLevel.next(levelId);
    this.placementScore.next(score);
    if (this.isBrowser()) {
      localStorage.setItem('currentLevel', levelId.toString());
      localStorage.setItem('placementScore', score.toString());
    }
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  private hasToken(): boolean {
    if (!this.isBrowser()) return false;
    return !!localStorage.getItem('token');
  }

  private getStoredUsername(): string {
    if (!this.isBrowser()) return '';
    return localStorage.getItem('username') || 'User';
  }

  login(token: string, username: string) {
    if (this.isBrowser()) {
      localStorage.setItem('token', token);
      localStorage.setItem('username', username);
    }
    this.loggedIn.next(true);
    this.username.next(username);
    this.fetchProfile();
    this.fetchUserInfo(username);
  }

  fetchProfile() {
    if (!this.isBrowser()) return;
    const token = localStorage.getItem('token');
    if (!token) return;

    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    this.http.get<any>('http://localhost:8086/api/users/me', { headers }).subscribe({
      next: (data) => {
        if (this.isBrowser()) {
          if (data.uid || data.id_user) {
            const id = data.uid || data.id_user;
            localStorage.setItem('userId', id.toString());
            this.userId.next(id);
          }
          if (data.email) {
            localStorage.setItem('email', data.email);
            this.email.next(data.email);
          }
          if (data.username) {
              localStorage.setItem('username', data.username);
              this.username.next(data.username);
              this.fetchUserInfo(data.username);
          }
        }
      },
   error: (err) => {
          console.error("Failed to fetch profile", err);
          if (err.status === 401) {
            this.logout();
          }
        }
    });

  }

  logout() {
    if (this.isBrowser()) {
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      localStorage.removeItem('email');
      localStorage.removeItem('userId');
      localStorage.removeItem('currentLevel');
      localStorage.removeItem('placementScore');
      localStorage.removeItem('completedLessons');
      localStorage.removeItem('totalLessons');
    }
    this.loggedIn.next(false);
    this.username.next('');
    this.email.next('');
    this.userId.next(null);
    this.currentLevel.next(1);
    this.placementScore.next(null);
    this.completedLessons.next(0);
    this.totalLessons.next(0);
    this.router.navigate(['/']);
  }

  isLoggedIn(): boolean {
    return this.loggedIn.value;
  }

  getCurrentLevel(): number {
    return this.currentLevel.value;
  }
}
