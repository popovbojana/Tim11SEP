import { TestBed } from '@angular/core/testing';

import { OffersApi } from './offers-api';

describe('OffersApi', () => {
  let service: OffersApi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OffersApi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
