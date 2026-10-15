import { Component } from '@angular/core';
import { Categoria } from '../../../shared';
import { RouterLink } from '@angular/router';
import { CategoriaService } from '../../../services/categoria.service';

@Component({
  imports: [RouterLink],
  selector: 'app-funcionario-categorias',
  styleUrl: './funcionario-categorias.css',
  templateUrl: './funcionario-categorias.html',
})
export class FuncionarioCategorias {

  categorias: Categoria[] = [];

  constructor(private categoriaService: CategoriaService) {}

  ngOnInit(): void {
    this.carregarCategorias();
  }

  carregarCategorias(): void {
    this.categoriaService.listar().subscribe({
      next: data => this.categorias = data,
      error: () => window.alert('Não foi possível carregar as categorias.')
    });
  }

  excluirCategoria(id: number): void {
    if (confirm('Tem certeza que deseja excluir esta categoria?')) {
      this.categoriaService.remover(id).subscribe({
        next: () => {
          this.categorias = this.categorias.filter(c => c.id !== id);
          window.alert(`Categoria #${id} excluída com sucesso!`);
        },
        error: () => window.alert('Não foi possível excluir a categoria.')
      });
    }
  }
}
