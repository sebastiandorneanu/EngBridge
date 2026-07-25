import { Component } from '@angular/core';
import { AuthService } from '../shared/auth.service';
import { Observable } from 'rxjs';
import { AsyncPipe, NgIf } from '@angular/common';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [AsyncPipe, NgIf],
  templateUrl: './footer.html',
  styleUrl: './footer.css',
})
export class FooterComponent {
  isLoggedIn$: Observable<boolean>;

  constructor(private authService: AuthService) {
    this.isLoggedIn$ = this.authService.isLoggedIn$;
  }
}
