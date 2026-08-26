import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormGroup } from "@angular/forms";
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-autocadastro',
  styleUrl: './autocadastro.css',
  templateUrl: './autocadastro.html',
})
export class Autocadastro {
  
  private http = inject(HttpClient)
  cepNaoEncontrado: boolean = false
  erroRequisicao: boolean = false
  
  cadastroForm = new FormGroup({
    cpf: new FormControl('',
      [
        Validators.required,
        Validators.minLength(11),
        Validators.maxLength(11)
      ]
    ),
    email: new FormControl('',
      [
        Validators.required,
        Validators.email
      ]
    ),
    nome: new FormControl('', Validators.required),
    
    endereco: new FormGroup({
      cep: new FormControl('', 
        [
          Validators.required,
          Validators.minLength(8),
          Validators.maxLength(8)
        ]
      ),
      logradouro: new FormControl('', Validators.required),
      numero: new FormControl('', Validators.required),
      complemento: new FormControl(''),
      bairro: new FormControl('', Validators.required),
      cidade: new FormControl('', Validators.required),
      uf: new FormControl('', Validators.required),
    }),

    telefone: new FormControl('', Validators.required)
  })

  onSubmit(){
    console.log(this.cadastroForm.value)
  }

  temErro(formControl: string, nomeErro: string): boolean {
    const campo = this.cadastroForm.get(formControl)
    if (campo === null){
      return false
    }
    return campo.hasError(nomeErro) && (campo.touched || campo.dirty)

  }

  buscarCep(){

    const cep = this.cadastroForm.get('endereco.cep')?.value

    if (cep?.length !== 8){
      return
    }

    this.http.get<CamposEndereco>(`https://viacep.com.br/ws/${cep}/json/`).subscribe({
      next: (resposta) => {
        this.erroRequisicao = false
        this.cepNaoEncontrado = false
        if (resposta.erro){
          this.cepNaoEncontrado = true
        }else{
          this.cadastroForm.patchValue({endereco: {logradouro: resposta.logradouro, 
                                  bairro: resposta.bairro, 
                                  cidade: resposta.localidade, 
                                  uf: resposta.uf}})
        }
      },
      error: (err) => {
        this.erroRequisicao = true
      } 
    });
    
  }

}
