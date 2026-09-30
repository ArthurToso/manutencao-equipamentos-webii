import { Component } from '@angular/core';
import { UsuarioDTO } from '../../../shared';
import { RouterLink } from '@angular/router';

const mockFuncionarios: UsuarioDTO[] = [
  { id: 1, nome: 'Carlos Silva', email: "carlossilva@gmail.com", perfil: 'FUNCIONARIO' },
  { id: 2, nome: 'Silva Carlos', email: "silvacarlos@gmail.com", perfil: 'FUNCIONARIO' }
];

@Component({
  imports: [RouterLink],
  selector: 'app-funcionario-funcionarios',
  styleUrl: './funcionario-funcionarios.css',
  templateUrl: './funcionario-funcionarios.html',
})

export class FuncionarioFuncionarios {

  funcionarios: UsuarioDTO[] = []

  ngOnInit(): void {
    this.carregarFuncionarios();
  }

  carregarFuncionarios(): void {
    const data = mockFuncionarios;
    this.funcionarios = data
  }

  excluirFuncionario(id: number | undefined): void {
    if (id === undefined) return;
    if (confirm('Tem certeza que deseja excluir esta categoria?')) {
      if (this.funcionarios.length === 1){
        window.alert('É necessário ter pelo menos 1 funcionário cadastrado!')
        return
      }
      this.funcionarios = this.funcionarios.filter(c => c.id !== id);
      window.alert(`Categoria #${id} excluída com sucesso!`);
    }
  }
}
