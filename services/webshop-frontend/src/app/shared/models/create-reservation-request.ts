export interface CreateReservationRequest {
  offerId: number;
  startDate: string;
  endDate: string;
  selectedAdditionalServiceIds?: number[];
}
