import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../shared/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-c1',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './c1.html',
  styleUrl: './c1.css',
})
export class C1Component {
  constructor(private authService: AuthService, private router: Router) {}

  startLesson() {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/courses-learn/3']);
    } else {
      this.router.navigate(['/login']);
    }
  }
}
