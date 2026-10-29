import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Categoria, NovaSolicitacaoDTO } from '../../../shared';
import { CategoriaService } from '../../../services/categoria.service';
import { SolicitacaoService } from '../../../services/solicitacao.service';

@Component({
  selector: 'app-cliente-nova-solicitacao',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './cliente-nova-solicitacao.html',
  styleUrl: './cliente-nova-solicitacao.css'
})
export class ClienteNovaSolicitacao {
  form: FormGroup;
  submitting = signal(false);
  erro = signal<string | null>(null);
  categorias = signal<Categoria[]>([]);

  readonly limiteDescricaoDefeito = 500;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private categoriaService: CategoriaService,
    private solicitacaoService: SolicitacaoService
  ) {
    this.form = this.fb.group({
      equipamento: ['', [Validators.required, Validators.maxLength(100)]],
      categoria: [null, Validators.required],
      defeito: ['', [Validators.required, Validators.maxLength(this.limiteDescricaoDefeito)]]
    });
  }

  ngOnInit(): void {
    this.categoriaService.listar().subscribe({
      next: (lista) => this.categorias.set(lista),
      error: (err) => this.erro.set(err.error?.mensagem ?? 'Não foi possível carregar as categorias.')
    });
  }

  get equipamento() { return this.form.get('equipamento'); }
  get categoria() { return this.form.get('categoria'); }
  get defeito() { return this.form.get('defeito'); }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { equipamento, categoria, defeito } = this.form.value;

    const dados: NovaSolicitacaoDTO = {
      descricaoEquipamento: equipamento,
      categoriaId: categoria,
      descricaoDefeito: defeito
    };

    this.submitting.set(true);
    this.erro.set(null);

    this.solicitacaoService.criar(dados).subscribe({
      next: () => this.router.navigate(['/cliente/home']),
      error: (err) => {
        this.erro.set(err.error?.mensagem ?? 'Não foi possível registrar a solicitação.');
        this.submitting.set(false);
      }
    });
  }
}