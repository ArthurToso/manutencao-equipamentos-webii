import { Component } from '@angular/core';
import { FuncionarioDTO } from '../../../shared';
import { RouterLink } from '@angular/router';

const mockFuncionarios: FuncionarioDTO[] = [
  { id: 1, nome: 'Carlos Silva', email: "carlossilva@gmail.com", perfil: 'FUNCIONARIO', dtNasc: "01-01-2001" },
  { id: 2, nome: 'Silva Carlos', email: "silvacarlos@gmail.com", perfil: 'FUNCIONARIO', dtNasc: "02-02-2002" }
];

@Component({
  imports: [RouterLink],
  selector: 'app-funcionario-funcionarios',
  styleUrl: './funcionario-funcionarios.css',
  templateUrl: './funcionario-funcionarios.html',
})

export class FuncionarioFuncionarios {

  funcionarios: FuncionarioDTO[] = []

  ngOnInit(): void {
    this.carregarFuncionarios();
  }

  carregarFuncionarios(): void {
    const data = mockFuncionarios;
    this.funcionarios = data
  }

  excluirFuncionario(id: number | undefined): void {
    if (id === undefined) return;
    if (this.funcionarios.length === 1){
        window.alert('É necessário ter pelo menos 1 funcionário cadastrado!')
        return
    }
    if (confirm('Tem certeza que deseja excluir este funcionário?')) {
      this.funcionarios = this.funcionarios.filter(c => c.id !== id);
      window.alert(`Funcionário #${id} excluído com sucesso!`);
    }
  }
}
