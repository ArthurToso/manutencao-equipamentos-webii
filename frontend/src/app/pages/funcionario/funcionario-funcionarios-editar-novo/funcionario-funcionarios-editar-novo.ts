import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FuncionarioCrudDTO, FuncionarioDTO } from '../../../shared';
import { NgbDatepickerModule, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';

@Component({
  imports: [ReactiveFormsModule, RouterLink, NgbDatepickerModule],
  selector: 'app-funcionario-funcionarios-editar-novo',
  styleUrl: './funcionario-funcionarios-editar-novo.css',
  templateUrl: './funcionario-funcionarios-editar-novo.html',
})
export class FuncionarioFuncionariosEditarNovo implements OnInit {
  form!: FormGroup;
  isEdicao: boolean = false;
  funcionarioId: number | null = null;
  hoje: NgbDateStruct;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router
  ) {
    const agora = new Date();
    this.hoje = {
      year: agora.getFullYear(),
      month: agora.getMonth() + 1,
      day: agora.getDate(),
    };
  }

  ngOnInit(): void {
    this.inicializarFormulario();
    this.verificarModoEdicao();
  }

  inicializarFormulario(): void {
    this.form = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      dtNasc: [null, Validators.required],
      senha: ['', [Validators.required, Validators.minLength(8)]],
    });
  }

  verificarModoEdicao(): void {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam) {
      this.isEdicao = true;
      this.funcionarioId = Number(idParam);

      const senha = this.form.get('senha');
      senha?.clearValidators();
      senha?.updateValueAndValidity();

      this.carregarDadosFuncionario(this.funcionarioId);
    }
  }

  carregarDadosFuncionario(id: number): void {
    // Mock temporário para simular dados existentes
    const funcionariosMock: FuncionarioDTO[] = [
      { id: 1, nome: 'Carlos Silva', email: 'carlossilva@gmail.com', perfil: 'FUNCIONARIO', dtNasc: '2001-01-01' },
      { id: 2, nome: 'Silva Carlos', email: 'silvacarlos@gmail.com', perfil: 'FUNCIONARIO', dtNasc: '2002-02-02' },
    ];

    const funcionario = funcionariosMock.find((f) => f.id === id);
    if (funcionario) {
      this.form.patchValue({
        nome: funcionario.nome,
        email: funcionario.email,
        dtNasc: this.isoParaData(funcionario.dtNasc),
      });
    }
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { nome, email, dtNasc, senha } = this.form.value;

    if (this.isEdicao) {
      const funcionario: FuncionarioDTO = {
        id: this.funcionarioId ?? undefined,
        nome,
        email,
        perfil: 'FUNCIONARIO',
        dtNasc: this.dataParaIso(dtNasc),
      };

      console.log(`Atualizando funcionário #${this.funcionarioId}:`, funcionario);
      window.alert(`Funcionário #${this.funcionarioId} atualizado com sucesso!`);
    } else {
      const funcionario: FuncionarioCrudDTO = {
        nome,
        email,
        perfil: 'FUNCIONARIO',
        dtNasc: this.dataParaIso(dtNasc),
        senha,
      };

      console.log('Criando novo funcionário:', { ...funcionario, senha: '***' });
      window.alert('Funcionário cadastrado com sucesso!');
    }

    this.router.navigate(['/funcionario/funcionarios']);
  }

  private isoParaData(iso: string): NgbDateStruct | null {
    const partes = iso?.split('-');
    if (partes?.length !== 3) {
      return null;
    }

    return {
      year: Number(partes[0]),
      month: Number(partes[1]),
      day: Number(partes[2]),
    };
  }

  private dataParaIso(data: NgbDateStruct): string {
    const mes = String(data.month).padStart(2, '0');
    const dia = String(data.day).padStart(2, '0');

    return `${data.year}-${mes}-${dia}`;
  }
}