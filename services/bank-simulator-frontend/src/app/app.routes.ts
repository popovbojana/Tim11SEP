import { Routes } from '@angular/router';
import { Checkout } from './features/components/checkout/checkout';
import { QrCheckout } from './features/components/qr-checkout/qr-checkout';

export const routes: Routes = [
  { path: 'checkout/:bankPaymentId', component: Checkout },
  { path: 'qr-checkout/:bankPaymentId', component: QrCheckout },
];
