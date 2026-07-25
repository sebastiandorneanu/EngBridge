import { ComponentFixture, TestBed } from '@angular/core/testing';

import { B2 } from './b2';

describe('B2', () => {
  let component: B2;
  let fixture: ComponentFixture<B2>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [B2]
    })
    .compileComponents();

    fixture = TestBed.createComponent(B2);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
