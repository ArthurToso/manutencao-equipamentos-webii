import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

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
    private router: Router
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
    // Mock temporário para simular dados existentes
    const categoriasMock = [
      { id: 1, nome: 'Notebooks e Laptops' },
      { id: 2, nome: 'Smartphones e Tablets' },
      { id: 3, nome: 'Impressoras' }
    ];

    const categoria = categoriasMock.find(c => c.id === id);
    if (categoria) {
      this.form.patchValue({
        nome: categoria.nome
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
      console.log(`Atualizando categoria #${this.categoriaId}:`, dadosFormulario);
      window.alert(`Categoria #${this.categoriaId} atualizada com sucesso!`);
    } else {
      console.log('Criando nova categoria:', dadosFormulario);
      window.alert('Categoria cadastrada com sucesso!');
    }

    this.router.navigate(['/funcionario/categorias']);
  }

}