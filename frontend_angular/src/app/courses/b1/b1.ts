import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../shared/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-b1',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './b1.html',
  styleUrl: './b1.css',
})
export class B1Component {
  constructor(private authService: AuthService, private router: Router) {}

  startLesson() {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/courses-learn/1']);
    } else {
      this.router.navigate(['/login']);
    }
  }
}
