import { Routes } from '@angular/router';
import { OffersList } from './features/offers/offers-list/offers-list';
import { OfferDetails } from './features/offers/offer-details/offer-details';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { MyReservations } from './features/reservations/my-reservations/my-reservations';
import { CreateReservation } from './features/reservations/create-reservation/create-reservation';
import { Success } from './features/payments/success/success';
import { Failed } from './features/payments/failed/failed';
import { Error } from './features/payments/error/error';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'offers' },

  { path: 'payment/success', component: Success },
  { path: 'payment/failed', component: Failed },
  { path: 'payment/error', component: Error },

  { path: 'offers', component: OffersList },
  { path: 'offers/:id/reserve', component: CreateReservation },
  { path: 'offers/:id', component: OfferDetails },

  { path: 'auth/login', component: Login },
  { path: 'auth/register', component: Register },

  { path: 'reservations', component: MyReservations },

  { path: '**', redirectTo: 'offers' },
];
