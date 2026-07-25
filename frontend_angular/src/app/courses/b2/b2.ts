import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../shared/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-b2',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './b2.html',
  styleUrl: './b2.css',
})
export class B2Component {
  constructor(private authService: AuthService, private router: Router) {}

  startLesson() {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/courses-learn/2']);
    } else {
      this.router.navigate(['/login']);
    }
  }
}
