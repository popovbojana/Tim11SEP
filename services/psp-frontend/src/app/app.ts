import { Component, inject, signal } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { Navbar } from './shared/components/navbar/navbar';
import { filter, map } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Navbar],
  template: `
    @if (showNavbar()) {
      <app-navbar></app-navbar>
    }

    <main [class.page]="showNavbar()">
      <div [class.container]="showNavbar()">
        <router-outlet></router-outlet>
      </div>
    </main>
  `,
  styles: [
    `
      .page {
        min-height: calc(100dvh - 50px);
        background: #fafafa;
        padding: 24px 16px;
        box-sizing: border-box;
      }

      .container {
        max-width: 1100px;
        margin: 0 auto;
      }
    `,
  ],
})
export class App {
  private router = inject(Router);
  protected readonly title = signal('psp-frontend');

  showNavbar = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map((event: NavigationEnd) => !event.urlAfterRedirects.includes('/checkout'))
    ),
    { initialValue: true }
  );
}