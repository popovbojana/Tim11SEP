import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MerchantApi } from '../../../../core/services/merchant-api/merchant-api';
import { MerchantResponse } from '../../../../shared/models/merchant';

@Component({
  selector: 'app-merchants',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './merchants-list.html',
  styleUrl: './merchants-list.scss',
})
export class MerchantsList implements OnInit {
  private merchantApi = inject(MerchantApi);

  merchants = signal<MerchantResponse[] | null>(null);
  errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.errorMessage.set(null);
    this.merchants.set(null);

    this.merchantApi.getAll().subscribe({
      next: (res) => {
        this.merchants.set(Array.isArray(res) ? res : []);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Failed to load merchants.');
        this.merchants.set([]);
      },
    });
  }

  trackByMerchantKey(_: number, item: MerchantResponse): string {
    return item.merchantKey;
  }
}
