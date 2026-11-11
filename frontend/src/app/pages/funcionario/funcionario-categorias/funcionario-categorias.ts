import { ChangeDetectorRef, inject, Component } from '@angular/core';
import { Categoria } from '../../../shared';
import { RouterLink } from '@angular/router';
import { CategoriaService } from '../../../services/categoria.service';
import { finalize } from 'rxjs';

@Component({
  imports: [RouterLink],
  selector: 'app-funcionario-categorias',
  styleUrl: './funcionario-categorias.css',
  templateUrl: './funcionario-categorias.html',
})
export class FuncionarioCategorias {

  private cdr = inject(ChangeDetectorRef);
  categorias: Categoria[] = [];

  constructor(private categoriaService: CategoriaService) {}

  ngOnInit(): void {
    this.carregarCategorias();
  }

  carregarCategorias(): void {
    this.categoriaService.listar().pipe(
      finalize(() => this.cdr.detectChanges())
    ).subscribe({
      next: data => this.categorias = data,
      error: () => window.alert('Não foi possível carregar as categorias.')
    });
  }

  excluirCategoria(id: number): void {
    if (confirm('Tem certeza que deseja excluir esta categoria?')) {
      this.categoriaService.remover(id).subscribe({
        next: () => {
          this.categorias = this.categorias.filter(c => c.id !== id);
          this.cdr.detectChanges();
          window.alert(`Categoria #${id} excluída com sucesso!`);
        },
        error: () => window.alert('Não foi possível excluir a categoria.')
      });
    }
  }
}
