import { TestBed } from '@angular/core/testing';

import { PaymentMethodApi } from './payment-method-api';

describe('PaymentMethodApi', () => {
  let service: PaymentMethodApi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PaymentMethodApi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
