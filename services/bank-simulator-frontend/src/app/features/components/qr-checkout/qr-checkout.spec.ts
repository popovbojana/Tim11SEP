import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QrCheckout } from './qr-checkout';

describe('QrCheckout', () => {
  let component: QrCheckout;
  let fixture: ComponentFixture<QrCheckout>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QrCheckout]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QrCheckout);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
