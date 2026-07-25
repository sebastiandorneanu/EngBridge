import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { CoursesLearnComponent } from './courses-learn';

describe('CoursesLearnComponent', () => {
  let component: CoursesLearnComponent;
  let fixture: ComponentFixture<CoursesLearnComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CoursesLearnComponent],
      providers: [provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CoursesLearnComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});