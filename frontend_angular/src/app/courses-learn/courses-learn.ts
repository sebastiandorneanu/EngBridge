import { ChangeDetectorRef, Component, OnInit, NgZone } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { AuthService } from '../shared/auth.service';

@Component({
  selector: 'app-courses-learn',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './courses-learn.html',
  styleUrl: './courses-learn.css'
})
export class CoursesLearnComponent implements OnInit {
  levelId: string | null = null;
  courses: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private router: Router,
    private authService: AuthService,
    private cd: ChangeDetectorRef,
    private ngZone: NgZone
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.levelId = params.get('levelId');
      console.log('2. Level ID from URL:', this.levelId);
      if (this.levelId) {
        console.log('3. Fetching courses for ID:', this.levelId);
        this.fetchCourses(this.levelId);
      }
      else
      {
        console.warn('3. No Level ID found!');
      }
    });
  }

  fetchCourses(id: string) {
    this.http.get<any[]>(`http://localhost:8081/levels/${id}/courses`)
      .subscribe({
        next: (data) =>
        {
          this.ngZone.run(() => {
             console.log('data fetched from server:', data);
             this.courses = data;
             this.cd.detectChanges(); // Extra safety
          });
        },
        error: (err) => console.error('Loading error:', err)
      });
  }

  startLesson(courseId: number) {
    if (this.authService.isLoggedIn()) {
      this.router.navigate([`/${this.levelId}/${courseId}`]);
    } else {
      this.router.navigate(['/login']);
    }
  }

  resetProgress(courseId: number) {
    if (!confirm('Are you sure you want to reset your progress for this course? This action cannot be undone.')) {
      return;
    }

    const userId = localStorage.getItem('userId');
    if (!userId) {
      alert('You must be logged in to reset progress.');
      return;
    }

    this.http.delete(`http://localhost:8081/progress/reset?userId=${userId}&courseId=${courseId}`, { responseType: 'text' })
      .subscribe({
        next: (res) => {
          this.ngZone.run(() => {
            alert('Progress reset successfully!');
          });
        },
        error: (err) => {
          console.error('Reset error:', err);
          alert('Failed to reset progress.');
        }
      });
  }
}