import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PaymentMethodApi } from '../../../../core/services/payment-method-api/payment-method-api';
import { toSignal } from '@angular/core/rxjs-interop';
import { startWith } from 'rxjs';

@Component({
  selector: 'app-payment-method-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './payment-method-edit.html',
  styleUrl: './payment-method-edit.scss',
})
export class PaymentMethodEdit implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private paymentMethodApi = inject(PaymentMethodApi);

  id!: number;
  
  fetching = signal(true);
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  initialValues = signal<{ name: string; serviceName: string } | null>(null);

  form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9_-]+$/)]],
    serviceName: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9_-]+$/)]],
  });

  formValue = toSignal(
    this.form.valueChanges.pipe(startWith(this.form.getRawValue()))
  );

  isUnchanged = computed(() => {
    const initial = this.initialValues();
    const current = this.formValue();

    if (!initial || !current) return true;
    
    return current.name === initial.name && current.serviceName === initial.serviceName;
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = +idParam;
      this.loadMethod();
    }
  }

  loadMethod(): void {
    this.fetching.set(true);
    this.paymentMethodApi.findById(this.id).subscribe({
      next: (res) => {
        const data = { name: res.name, serviceName: res.serviceName };
        this.initialValues.set(data);
        this.form.patchValue(data);
        this.fetching.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Failed to load payment method.');
        this.fetching.set(false);
      },
    });
  }

  submit(): void {
    if (this.form.invalid || this.isUnchanged()) return;

    this.loading.set(true);
    const payload = this.form.getRawValue();
    payload.name = payload.name.toUpperCase().trim();
    payload.serviceName = payload.serviceName.toUpperCase().trim();

    this.paymentMethodApi.update(this.id, payload).subscribe({
      next: () => this.router.navigate(['/payment-methods']),
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message ?? 'Update failed.');
      },
    });
  }
}