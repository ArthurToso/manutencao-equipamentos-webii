import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CategoriaService } from '../../../services/categoria.service';

@Component({
  selector: 'app-funcionario-categoria-editar-novo',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './funcionario-categorias-editar-novo.html',
  styleUrls: ['./funcionario-categorias-editar-novo.css']
})
export class FuncionarioCategoriasEditarNovo implements OnInit {

  form!: FormGroup;
  isEdicao: boolean = false;
  categoriaId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private categoriaService: CategoriaService
  ) {}

  ngOnInit(): void {
    this.inicializarFormulario();
    this.verificarModoEdicao();
  }

  inicializarFormulario(): void {
    this.form = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(2)]]
    });
  }

  verificarModoEdicao(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    
    if (idParam) {
      this.isEdicao = true;
      this.categoriaId = Number(idParam);
      
      this.carregarDadosCategoria(this.categoriaId);
    }
  }

  carregarDadosCategoria(id: number): void {
    this.categoriaService.listar().subscribe({
      next: categorias => {
        const categoria = categorias.find(c => c.id === id);
        if (categoria) {
          this.form.patchValue({ nome: categoria.nome });
        } else {
          window.alert('Categoria não encontrada.');
          this.router.navigate(['/funcionario/categorias']);
        }
      },
      error: () => window.alert('Não foi possível carregar a categoria.')
    });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const nome = this.form.value.nome.trim();

    if (this.isEdicao && this.categoriaId) {
      this.categoriaService.atualizar(this.categoriaId, nome).subscribe({
        next: () => {
          window.alert(`Categoria #${this.categoriaId} atualizada com sucesso!`);
          this.router.navigate(['/funcionario/categorias']);
        },
        error: () => window.alert('Não foi possível atualizar a categoria.')
      });
    } else {
      this.categoriaService.criar(nome).subscribe({
        next: () => {
          window.alert('Categoria cadastrada com sucesso!');
          this.router.navigate(['/funcionario/categorias']);
        },
        error: () => window.alert('Não foi possível cadastrar a categoria.')
      });
    }
  }

}