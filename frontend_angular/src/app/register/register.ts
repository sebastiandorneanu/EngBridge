import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class RegisterComponent {
  user = {
    username: '',
    password: '',
    email: '',
    first_name: '',
    last_name: ''
  };
  errorMessage: string = '';
  successMessage: string = '';

  constructor(private http: HttpClient, private router: Router) {}

  onSubmit() {
    this.errorMessage = '';
    this.successMessage = '';

    // Basic validation
    if (!this.user.username || !this.user.password || !this.user.email) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }

    const apiUrl = 'http://localhost:8086/api/auth/register';

    this.http.post<any>(apiUrl, this.user).subscribe({
      next: (response) => {
        // The backend returns { message: "..." } on success
        if (response.message) {
            this.successMessage = response.message;
            setTimeout(() => {
                this.router.navigate(['/']); 
            }, 2000);
        } else {
             this.errorMessage = 'Registration failed.';
        }
      },
      error: (error) => {
        console.error('Registration error:', error);
        console.error('Error body:', JSON.stringify(error.error));
        if (error.status === 0) {
            this.errorMessage = 'Unable to connect to the server. Please ensure the backend is running.';
        } else if (error.error && error.error.error) {
            this.errorMessage = error.error.error;
        } else if (error.error && error.error.message) {
            this.errorMessage = error.error.message;
        } else {
            this.errorMessage = 'An error occurred during registration. Please try again.';
        }
      }
    });
  }
}
