// course-redirect.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-course-redirect',
  template: '<div class="loader">Loading course...</div>'
})
export class CourseRedirectComponent implements OnInit {
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient
  ) {}

  ngOnInit() {
    const levelId = this.route.snapshot.paramMap.get('levelId');
    const courseId = this.route.snapshot.paramMap.get('courseId');

    if (courseId) {
      this.http.get<any[]>(`http://localhost:8081/courses/${courseId}/sections`)
        .subscribe({
          next: (sections) => {
            if (sections.length > 0) {

              const firstSectionId = sections[0].id;

              this.router.navigate([`/${levelId}/${courseId}/${firstSectionId}`]);
            }
          },
          error: (err) => console.error("Could not find sections", err)
        });
    }
  }
}
