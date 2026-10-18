import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-funcionario-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './funcionario-layout.html',
  styleUrl: './funcionario-layout.css'
})
export class FuncionarioLayout {
  private authService = inject(AuthService);
  private router = inject(Router);

  readonly nome = computed(() => this.authService.usuarioAtual()?.nome ?? '');

  sair(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
