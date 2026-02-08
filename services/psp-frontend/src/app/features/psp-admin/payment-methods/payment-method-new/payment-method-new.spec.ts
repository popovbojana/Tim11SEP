import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PaymentMethodNew } from './payment-method-new';

describe('PaymentMethodNew', () => {
  let component: PaymentMethodNew;
  let fixture: ComponentFixture<PaymentMethodNew>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaymentMethodNew]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PaymentMethodNew);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
