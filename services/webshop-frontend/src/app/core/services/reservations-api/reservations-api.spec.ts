import { TestBed } from '@angular/core/testing';

import { ReservationsApi } from './reservations-api';

describe('ReservationsApi', () => {
  let service: ReservationsApi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ReservationsApi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
