import { Routes } from '@angular/router';

import { PspLogin } from './features/psp-admin/login/psp-login';

import { MerchantsList } from './features/psp-admin/merchants/merchants-list/merchants-list';
import { MerchantCreate } from './features/psp-admin/merchants/merchant-create/merchant-create';
import { MerchantDetails } from './features/psp-admin/merchants/merchant-details/merchant-details';
import { Checkout } from './features/checkout/checkout/checkout';
import { PaymentMethodsList } from './features/psp-admin/payment-methods/payment-methods-list/payment-methods-list';
import { PaymentMethodNew } from './features/psp-admin/payment-methods/payment-method-new/payment-method-new';
import { PaymentMethodEdit } from './features/psp-admin/payment-methods/payment-method-edit/payment-method-edit';

export const routes: Routes = [
  { path: 'login', component: PspLogin },
  { path: '', pathMatch: 'full', redirectTo: 'merchants' },
  { path: 'merchants', component: MerchantsList },
  { path: 'merchants/new', component: MerchantCreate },
  { path: 'merchants/:merchantKey', component: MerchantDetails },
  { path: 'checkout/:paymentId', component: Checkout },
  { path: 'payment-methods', component: PaymentMethodsList },
  { path: 'payment-methods/new', component: PaymentMethodNew },
  { path: 'payment-methods/edit/:id', component: PaymentMethodEdit}
];
