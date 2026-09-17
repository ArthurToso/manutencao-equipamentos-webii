import { Component } from '@angular/core';
import { Categoria } from '../../../shared';
import { RouterLink } from '@angular/router';

const mockCategorias: Categoria[] = [
  { id: 1, nome: 'Notebooks e Laptops' },
  { id: 2, nome: 'Smartphones e Tablets' },
  { id: 3, nome: 'Impressoras' }
];

@Component({
  imports: [RouterLink],
  selector: 'app-funcionario-categorias',
  styleUrl: './funcionario-categorias.css',
  templateUrl: './funcionario-categorias.html',
})
export class FuncionarioCategorias {

  categorias: Categoria[] = [];

  ngOnInit(): void {
    this.carregarCategorias();
  }

  carregarCategorias(): void {
    // Simulando o carregamento (igual ao seu mock de solicitações)
    const data = mockCategorias;

    if (data) {
      this.categorias = data.sort((a, b) => a.id - b.id);
    }
  }

  excluirCategoria(id: number): void {
    if (confirm('Tem certeza que deseja excluir esta categoria?')) {
      // Atualização direta do array mantendo o padrão simples
      this.categorias = this.categorias.filter(c => c.id !== id);
      window.alert(`Categoria #${id} excluída com sucesso!`);
    }
  }
}
