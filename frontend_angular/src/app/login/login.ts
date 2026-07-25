import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../shared/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  loginData = {
    username: '',
    password: '',
  };

  constructor(private http: HttpClient, private router: Router, private authService: AuthService) {}

  onLogin() {
    this.http.post('http://localhost:8086/api/auth/login', this.loginData).subscribe({
      next: (response: any) => {
        if (response.token) {
          this.authService.login(response.token, this.loginData.username);
          this.router.navigate(['/']);
        }
      },
      error: (err) => {
        alert(err.error?.error || 'Login failed');
      },
    });
  }
}
