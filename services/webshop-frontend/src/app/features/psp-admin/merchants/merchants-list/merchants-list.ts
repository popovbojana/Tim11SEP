import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

type MerchantRow = {
  merchantKey: string;
  activeMethods: string[];
};

@Component({
  selector: 'app-psp-merchants-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './merchants-list.html',
  styleUrl: './merchants-list.scss',
})
export class MerchantsList {
  merchants: MerchantRow[] = [
    {
      merchantKey: 'MERCH_001',
      activeMethods: ['CARD', 'QR'],
    },
    {
      merchantKey: 'MERCH_002',
      activeMethods: ['PAYPAL'],
    },
    {
      merchantKey: 'MERCH_003',
      activeMethods: ['CRYPTO', 'CARD'],
    },
  ];
}
