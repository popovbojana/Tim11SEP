import { ReservationStatus } from './reservation-status';
import { Vehicle } from './vehicle';
import { InsurancePackage } from './insurance-package';
import { AdditionalService } from './additional-service';

export interface Reservation {
  id: number;
  customerEmail: string;
  offerId: number;
  offerTitle: string;
  startDate: string;
  endDate: string;
  status: ReservationStatus;
  totalPrice: number;
  currency: string;
  paymentMethod: string;
  paymentReference: string | null;
  paidAt: string | null;
  createdAt: string;
  vehicle: Vehicle;
  insurancePackage: InsurancePackage;
  additionalServices: AdditionalService[];
  merchantOrderId: string;
  pspPaymentId: number;
}
