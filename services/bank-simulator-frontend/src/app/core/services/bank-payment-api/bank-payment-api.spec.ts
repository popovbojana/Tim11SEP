import { TestBed } from '@angular/core/testing';

import { BankPaymentApi } from './bank-payment-api';

describe('BankPaymentApi', () => {
  let service: BankPaymentApi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BankPaymentApi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
