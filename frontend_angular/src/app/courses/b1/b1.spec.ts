import { ComponentFixture, TestBed } from '@angular/core/testing';

import { B1 } from './b1';

describe('B1', () => {
  let component: B1;
  let fixture: ComponentFixture<B1>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [B1]
    })
    .compileComponents();

    fixture = TestBed.createComponent(B1);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
