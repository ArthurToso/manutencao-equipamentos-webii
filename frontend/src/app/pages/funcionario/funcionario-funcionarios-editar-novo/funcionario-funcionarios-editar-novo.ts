import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FuncionarioCrudDTO } from '../../../shared';
import { FuncionarioService } from '../../../services/funcionario-service';
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
    private router: Router,
    private funcionarioService: FuncionarioService
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
    this.funcionarioService.buscarPorId(id).subscribe({
      next: (funcionario) => {
        this.form.patchValue({
          nome: funcionario.nome,
          email: funcionario.email,
          dtNasc: this.isoParaData(funcionario.dataNascimento),
        });
      },
      error: (err) => {
        window.alert(err.error?.mensagem ?? 'Não foi possível carregar o funcionário.');
        this.router.navigate(['/funcionario/funcionarios']);
      },
    });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { nome, email, dtNasc, senha } = this.form.value;

    const dados: FuncionarioCrudDTO = {
      nome,
      email,
      dataNascimento: this.dataParaIso(dtNasc),
    };
    if (senha) {
      dados.senha = senha;
    }

    const requisicao = this.isEdicao && this.funcionarioId !== null
      ? this.funcionarioService.atualizar(this.funcionarioId, dados)
      : this.funcionarioService.criar(dados);

    requisicao.subscribe({
      next: () => {
        window.alert(this.isEdicao
          ? 'Funcionário atualizado com sucesso!'
          : 'Funcionário cadastrado com sucesso!');
        this.router.navigate(['/funcionario/funcionarios']);
      },
      error: (err) => {
        window.alert(err.error?.mensagem ?? 'Não foi possível salvar o funcionário.');
      },
    });
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