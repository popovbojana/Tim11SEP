import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from './shared/components/navbar/navbar';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Navbar],
  template: `
    <app-navbar></app-navbar>

    <main class="page">
      <div class="container">
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
  protected readonly title = signal('webshop-frontend');
}
