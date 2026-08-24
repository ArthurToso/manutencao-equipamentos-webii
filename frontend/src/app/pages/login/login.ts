import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  form: FormGroup;
  submitting = false;
  loginError: string | null = null;

  // Legenda de estados da solicitação (RF013), usada apenas como elemento visual
  // de identidade do sistema no painel lateral.
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

  constructor(private fb: FormBuilder, private router: Router) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      senha: ['', [Validators.required]]
    });
  }

  get email() {
    return this.form.get('email');
  }

  get senha() {
    return this.form.get('senha');
  }

  onSubmit(): void {
    this.loginError = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;

    // TODO: substituir pela chamada real ao endpoint RF002 (POST /auth/login)
    setTimeout(() => {
      this.submitting = false;
      // this.router.navigate(['/']);
    }, 800);
  }
}
