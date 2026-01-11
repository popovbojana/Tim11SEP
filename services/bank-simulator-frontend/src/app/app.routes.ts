import { Routes } from '@angular/router';
import { Checkout } from './features/components/checkout/checkout/checkout';

export const routes: Routes = [
    { path: 'checkout/:bankPaymentId', component: Checkout }
];
