import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TokenService } from '../../../core/services/token/token';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar {
  constructor(private tokenService: TokenService, private router: Router) {}

  isLoggedIn(): boolean {
    return this.tokenService.isLoggedIn();
  }

  logout(): void {
    this.tokenService.clear();
    this.router.navigate(['/login']);
  }
}