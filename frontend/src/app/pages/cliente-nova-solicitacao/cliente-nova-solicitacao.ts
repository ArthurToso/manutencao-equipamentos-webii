import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-cliente-nova-solicitacao',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './cliente-nova-solicitacao.html',
  styleUrl: './cliente-nova-solicitacao.scss'
})
export class ClienteNovaSolicitacao {
  form: FormGroup;
  submitting = false;

  // TODO: substituir por chamada real ao endpoint de categorias (RF017)
  // quando o backend estiver disponível.
  readonly categorias = ['Notebook', 'Desktop', 'Impressora', 'Mouse', 'Teclado'];

  readonly limiteDescricaoDefeito = 500;

  constructor(private fb: FormBuilder, private router: Router) {
    this.form = this.fb.group({
      // Sem limite de 30 caracteres aqui: esse limite (RF003) é sobre a EXIBIÇÃO
      // na listagem do cliente, não sobre o que pode ser digitado no cadastro.
      equipamento: ['', [Validators.required, Validators.maxLength(100)]],
      categoria: ['', Validators.required],
      defeito: ['', [Validators.required, Validators.maxLength(this.limiteDescricaoDefeito)]]
    });
  }

  get equipamento() {
    return this.form.get('equipamento');
  }

  get categoria() {
    return this.form.get('categoria');
  }

  get defeito() {
    return this.form.get('defeito');
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting = true;

    // TODO: substituir pela chamada real ao endpoint RF004
    // (POST /solicitacoes), que deve registrar data/hora e estado ABERTA.
    setTimeout(() => {
      this.submitting = false;
      // this.router.navigate(['/cliente/home']);
    }, 800);
  }
}
