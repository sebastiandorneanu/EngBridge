import { Routes } from '@angular/router';
import { HomeComponent } from './home/home';
import { B1Component } from './courses/b1/b1';
import { B2Component } from './courses/b2/b2';
import { C1Component } from './courses/c1/c1';
import { CoursesLearnComponent } from './courses-learn/courses-learn';
import { LessonViewComponent } from './courses-learn-view/courses-learn-view';
import { RegisterComponent } from './register/register';
import { LoginComponent } from './login/login';
import { CourseRedirectComponent } from './course-redirect/course-redirect';
import { PlacementTestComponent } from './placement-test/placement-test.component';
import { authGuard } from './shared/auth.guard';
import { levelGuard } from './shared/level.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'login', component: LoginComponent },
  { path: 'placement-test', component: PlacementTestComponent, canActivate: [authGuard] },
  
  // B1 Routes (Level 1 - Open to all logged-in users)
  { path: 'course-b1', component: B1Component, canActivate: [authGuard] },
  
  // B2 Routes (Level 2)
  { 
    path: 'course-b2', 
    component: B2Component, 
    canActivate: [authGuard, levelGuard], 
    data: { requiredLevel: 2 } 
  },
  
  // C1 Routes (Level 3)
  { 
    path: 'course-c1', 
    component: C1Component, 
    canActivate: [authGuard, levelGuard], 
    data: { requiredLevel: 3 } 
  },
  
  // Dynamic Courses Routes
  { 
    path: 'courses-learn/:levelId', 
    component: CoursesLearnComponent, 
    canActivate: [authGuard, levelGuard] 
  },
  
  // Redirect and Lesson Views
  { 
    path: ':levelId/:courseId', 
    component: CourseRedirectComponent, 
    canActivate: [authGuard, levelGuard] 
  },
  
  { 
    path: ':levelId/:courseId/:sectionId', 
    component: LessonViewComponent, 
    canActivate: [authGuard, levelGuard] 
  }
];