import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login implements OnInit {

  readonly estados = [
    { nome: 'Aberta', cor: '#8A93A3' },
    { nome: 'Orçada', cor: '#8C6A4F' },
    { nome: 'Rejeitada', cor: '#C0453A' },
    { nome: 'Aprovada', cor: '#C99A2E' },
    { nome: 'Redirecionada', cor: '#7A5FB0' },
    { nome: 'Arrumada', cor: '#3E6FB5' },
    { nome: 'Paga', cor: '#C97A3B' },
    { nome: 'Finalizada', cor: '#4C8C5E' }
  ];

  private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);

  submitting = false;
  loginError: string | null = null;

  form: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required]]
  });

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.redirecionarPorPerfil();
    }
  }

  get email() {
    return this.form.get('email');
  }

  get senha() {
    return this.form.get('senha');
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.form.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  onSubmit(): void {
    this.loginError = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;

    const email = this.form.value.email;
    const senha = this.form.value.senha;

    this.authService.login(email, senha).subscribe({
      next: (res) => {
        this.submitting = false;
        if (res.usuario?.perfil === 'FUNCIONARIO') {
          this.router.navigate(['/funcionario/home']);
        } else {
          this.router.navigate(['/cliente/home']);
        }
      },
      error: (err) => {
        this.submitting = false;
        if (err.status === 401) {
          this.loginError = err.error?.mensagem || err.error?.message || 'E-mail ou senha inválidos';
        } else {
          this.loginError = 'Não foi possível entrar. Tente novamente.';
        }
      }
    });
  }

  private redirecionarPorPerfil(): void {
    const perfil = this.authService.perfil();
    if (perfil === 'FUNCIONARIO') {
      this.router.navigate(['/funcionario/home']);
    } else {
      this.router.navigate(['/cliente/home']);
    }
  }
}
