import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CourseRedirect } from './course-redirect';

describe('CourseRedirect', () => {
  let component: CourseRedirect;
  let fixture: ComponentFixture<CourseRedirect>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CourseRedirect]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CourseRedirect);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
