import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CoursesLearnView } from './courses-learn-view';

describe('CoursesLearnView', () => {
  let component: CoursesLearnView;
  let fixture: ComponentFixture<CoursesLearnView>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CoursesLearnView]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CoursesLearnView);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
