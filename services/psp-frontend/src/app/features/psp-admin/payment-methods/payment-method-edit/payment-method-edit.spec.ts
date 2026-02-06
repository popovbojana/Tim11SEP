import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PaymentMethodEdit } from './payment-method-edit';

describe('PaymentMethodEdit', () => {
  let component: PaymentMethodEdit;
  let fixture: ComponentFixture<PaymentMethodEdit>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaymentMethodEdit]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PaymentMethodEdit);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
