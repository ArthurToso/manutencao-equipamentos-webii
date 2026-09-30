import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  selector: 'app-funcionario-funcionarios-editar-novo',
  styleUrl: './funcionario-funcionarios-editar-novo.css',
  templateUrl: './funcionario-funcionarios-editar-novo.html',
})
export class FuncionarioFuncionariosEditarNovo {
  form!: FormGroup;
  isEdicao: boolean = false;
  funcionarioId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router
  ){}

  ngOnInit(): void {
    this.inicializarFormulario();
    this.verificarModoEdicao();
  }

  inicializarFormulario(): void {
    this.form = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
    });
  }

  verificarModoEdicao(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    
    if (idParam) {
      this.isEdicao = true;
      this.funcionarioId = Number(idParam);
      
      this.carregarDadosCategoria(this.funcionarioId);
    }
  }

  carregarDadosCategoria(id: number): void {
    // Mock temporário para simular dados existentes
    const funcionariosMock = [
      { id: 1, nome: 'Carlos Silva', email: "carlossilva@gmail.com", perfil: 'FUNCIONARIO' },
      { id: 2, nome: 'Silva Carlos', email: "silvacarlos@gmail.com", perfil: 'FUNCIONARIO' }
    ];

    const funcionario = funcionariosMock.find(f => f.id === id);
    if (funcionario) {
      this.form.patchValue({
        nome: funcionario.nome,
        email: funcionario.email,
        perfil: funcionario.perfil
      });
    }
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const dadosFormulario = this.form.value;

    if (this.isEdicao) {
      console.log(`Atualizando funcionário #${this.funcionarioId}:`, dadosFormulario);
      window.alert(`Funcionário #${this.funcionarioId} atualizado com sucesso!`);
    } else {
      console.log('Criando novo funcionário:', dadosFormulario); 
      window.alert('Funcionário cadastrado com sucesso!');
    }

    this.router.navigate(['/funcionario/funcionarios']);
  }

}
