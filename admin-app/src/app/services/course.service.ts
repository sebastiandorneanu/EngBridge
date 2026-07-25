import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CourseService {
  private apiUrl = `http://localhost:8086/api`;

  constructor(private http: HttpClient) {}

  getCourses(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/courses`);
  }

  getSectionsByCourse(courseId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/sections/course/${courseId}`);
  }

  addExercise(sectionId: number, exercise: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/exercises/section/${sectionId}`, exercise);
  }

  deleteExercise(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/exercises/${id}`);
  }
}
