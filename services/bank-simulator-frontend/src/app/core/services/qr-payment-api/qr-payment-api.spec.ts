import { TestBed } from '@angular/core/testing';

import { QrPaymentApi } from './qr-payment-api';

describe('QrPaymentApi', () => {
  let service: QrPaymentApi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(QrPaymentApi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
