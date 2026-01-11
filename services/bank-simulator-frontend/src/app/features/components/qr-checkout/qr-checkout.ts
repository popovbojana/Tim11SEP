import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { QrPaymentApi } from '../../../core/services/qr-payment-api/qr-payment-api';

@Component({
  selector: 'app-qr-checkout',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './qr-checkout.html',
  styleUrl: './qr-checkout.scss',
})
export class QrCheckout {
  error = signal<string | null>(null);
  loading = signal<boolean>(false);

  bankPaymentId = signal<number | null>(null);
  qrImageSrc = signal<string | null>(null);

  constructor(private route: ActivatedRoute, private qrApi: QrPaymentApi) {
    this.route.paramMap.subscribe((params) => {
      const idStr = params.get('bankPaymentId');
      const id = idStr ? Number(idStr) : null;

      if (!id || Number.isNaN(id)) {
        this.error.set('Invalid bank payment id');
        return;
      }

      this.bankPaymentId.set(id);
      this.loadQr(id);
    });
  }

  private loadQr(id: number) {
    this.error.set(null);
    this.loading.set(true);
    this.qrImageSrc.set(null);

    this.qrApi.getQr(id).subscribe({
      next: (res) => {
        const src = `data:image/png;base64,${res.qrImageBase64}`;
        this.qrImageSrc.set(src);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Failed to load QR code.');
      },
    });
  }
}
