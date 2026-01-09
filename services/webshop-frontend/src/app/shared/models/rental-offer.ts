import { Vehicle } from './vehicle';
import { InsurancePackage } from './insurance-package';
import { AdditionalService } from './additional-service';

export interface RentalOffer {
  id: number;
  title: string;
  description: string;
  vehicle: Vehicle;
  insurancePackage: InsurancePackage;
  additionalServices: AdditionalService[];
  basePricePerDay: number;
}
