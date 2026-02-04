import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PaymentMethodsList } from './payment-methods-list';

describe('PaymentMethodsList', () => {
  let component: PaymentMethodsList;
  let fixture: ComponentFixture<PaymentMethodsList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PaymentMethodsList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PaymentMethodsList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
