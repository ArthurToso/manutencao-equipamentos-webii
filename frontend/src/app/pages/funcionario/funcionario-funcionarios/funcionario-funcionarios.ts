import { Component, inject, signal } from '@angular/core';
import { FuncionarioDTO } from '../../../shared';
import { RouterLink } from '@angular/router';
import { FuncionarioService } from '../../../services/funcionario-service';

@Component({
  imports: [RouterLink],
  selector: 'app-funcionario-funcionarios',
  styleUrl: './funcionario-funcionarios.css',
  templateUrl: './funcionario-funcionarios.html',
})

export class FuncionarioFuncionarios {

  private funcionarioService = inject(FuncionarioService)

  funcionarios =  signal<FuncionarioDTO[]>([])

  ngOnInit(): void {
    this.carregarFuncionarios();
  }

  carregarFuncionarios(): void {
    this.funcionarioService.listar().subscribe({
      next: (lista) => {this.funcionarios.set(lista)},
      error: (err) => {console.log(err)}
    })
  }

  excluirFuncionario(id: number | undefined): void {
    if (id === undefined) return;
    if (this.funcionarios.length === 1){
        window.alert('É necessário ter pelo menos 1 funcionário cadastrado!')
        return
    }
    if (confirm('Tem certeza que deseja excluir este funcionário?')) {
      this.funcionarioService.remover(id).subscribe({
        next: () => {
          this.funcionarios.update(lista => lista.filter(f => f.id !== id))
          window.alert(`Funcionário excluído com sucesso!`)
        },
        error: (err) => {
          const mensagem = err.error?.mensagem ?? 'Não foi possível excluir o funcionário.'
          window.alert(mensagem)
        }
      })
    }
  }
}
