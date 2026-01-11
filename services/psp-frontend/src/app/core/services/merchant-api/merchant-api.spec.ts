import { TestBed } from '@angular/core/testing';

import { MerchantApi } from './merchant-api';

describe('MerchantApi', () => {
  let service: MerchantApi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MerchantApi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
