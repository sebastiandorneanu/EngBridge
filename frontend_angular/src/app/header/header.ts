import { Component, OnInit, ChangeDetectorRef, NgZone, HostListener } from '@angular/core';
import { AuthService } from '../shared/auth.service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class HeaderComponent implements OnInit {
  isLoggedIn = false;
  username = '';
  email = '';
  placementScore: number | null = null;
  currentLevel: number = 1;
  completedLessons: number = 0;
  totalLessons: number = 0;

  coursesOpen = false;
  profileOpen = false;

  constructor(
    private authService: AuthService,
    private cd: ChangeDetectorRef,
    private ngZone: NgZone
  ) {}

  ngOnInit() {
    this.authService.isLoggedIn$.subscribe(status => {
      this.ngZone.run(() => {
        this.isLoggedIn = status;
        this.cd.detectChanges();
      });
    });

    this.authService.username$.subscribe(name => {
      this.ngZone.run(() => {
        this.username = name;
        this.cd.detectChanges();
      });
    });

    this.authService.email$.subscribe(mail => {
      this.ngZone.run(() => {
        this.email = mail;
        this.cd.detectChanges();
      });
    });

    this.authService.placementScore$.subscribe(score => {
      this.ngZone.run(() => {
        this.placementScore = score;
        this.cd.detectChanges();
      });
    });

    this.authService.currentLevel$.subscribe(level => {
      this.ngZone.run(() => {
        this.currentLevel = level;
        this.cd.detectChanges();
      });
    });

    this.authService.completedLessons$.subscribe(count => {
      this.ngZone.run(() => {
        this.completedLessons = count;
        this.cd.detectChanges();
      });
    });

    this.authService.totalLessons$.subscribe(count => {
      this.ngZone.run(() => {
        this.totalLessons = count;
        this.cd.detectChanges();
      });
    });
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    // If we click anywhere else, close the dropdowns
    // (This might be too broad if we click inside the menu, 
    // but the dropdown-toggle clicks have stopPropagation)
    if (this.coursesOpen || this.profileOpen) {
       this.ngZone.run(() => {
         this.coursesOpen = false;
         this.profileOpen = false;
         this.cd.detectChanges();
       });
    }
  }

  toggleCourses(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.ngZone.run(() => {
      this.coursesOpen = !this.coursesOpen;
      this.profileOpen = false;
      this.cd.detectChanges();
    });
  }

  toggleProfile(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.ngZone.run(() => {
      this.profileOpen = !this.profileOpen;
      this.coursesOpen = false;
      this.cd.detectChanges();
    });
  }

  logout() {
    this.authService.logout();
  }
}
