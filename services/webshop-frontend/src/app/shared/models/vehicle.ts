export type VehicleType =
  | 'CAR'
  | 'VAN'
  | 'SUV'
  | 'MOTORBIKE';

export interface Vehicle {
  id: number;
  type: VehicleType;
  brand: string;
  model: string;
  pricePerDay: number;
  description: string;
}
