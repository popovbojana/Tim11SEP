import { Routes } from '@angular/router';
import { Checkout } from './features/checkout/checkout/checkout';

export const routes: Routes = [
    { path: 'checkout/:paymentId', component: Checkout }
];
