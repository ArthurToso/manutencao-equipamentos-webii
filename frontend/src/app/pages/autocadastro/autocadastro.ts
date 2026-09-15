import { HttpClient } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormGroup } from "@angular/forms";
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgxMaskDirective, provideNgxMask } from 'ngx-mask';
import { ViaCepResponse } from './response';
import { RouterLink } from '@angular/router';
import { EstadoUF } from '../../shared/enums';
import { ClienteDTO } from '../../shared';

@Component({
  imports: [ReactiveFormsModule, NgxMaskDirective, RouterLink],
  selector: 'app-autocadastro',
  styleUrl: './autocadastro.css',
  templateUrl: './autocadastro.html',
  providers: [provideNgxMask({validation: false})]
})
export class Autocadastro {
  
  private http = inject(HttpClient)
  cepNaoEncontrado: boolean = false
  erroRequisicao: boolean = false
  cadastroConcluido: boolean   = false
  ufs = Object.entries(EstadoUF).map((sigla, estado) => ({sigla, estado}));
  
  cadastroForm = new FormGroup({
    cpf: new FormControl('',
      [
        Validators.required,
        Validators.minLength(11)
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
          Validators.minLength(8)
        ]
      ),
      logradouro: new FormControl('', Validators.required),
      numero: new FormControl('', Validators.required),
      complemento: new FormControl(''),
      bairro: new FormControl('', Validators.required),
      cidade: new FormControl('', Validators.required),
      uf: new FormControl('', Validators.required),
    }),

    telefone: new FormControl('', 
      [
        Validators.required, 
        Validators.pattern(/^(?:[0-9]{2}[2-5][0-9]{7}|[0-9]{2}9[0-9]{8})$/)
      ]
    )
  })

  onSubmit(){
    if(this.cadastroForm.valid){
      const form = this.cadastroForm.value;
      const newUser: ClienteDTO = {
        nome : form.nome ?? '',
        email : form.email ?? '',
        cpf : form.cpf ?? '',
        telefone : form.telefone ?? '',
        endereco: {
          cep : form.endereco?.cep ?? '',
          logradouro : form.endereco?.logradouro ?? '',
          numero : Number(form.endereco?.numero) ?? 0,
          complemento : form.endereco?.complemento ?? undefined,
          bairro : form.endereco?.bairro ?? '',
          cidade : form.endereco?.cidade ?? '',
          estado : form.endereco?.uf as EstadoUF ?? '' as EstadoUF
        }
      };
      console.log(newUser);
      this.cadastroConcluido = true
    }
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
      this.cepNaoEncontrado = false
      this.erroRequisicao = false
      this.cadastroForm.patchValue({endereco: {logradouro: '', 
                                  bairro: '', 
                                  cidade: '', 
                                  uf: ''}})
      return
    }


    this.cepNaoEncontrado = false
    this.erroRequisicao = false
    this.http.get<ViaCepResponse>(`https://viacep.com.br/ws/${cep}/json/`).subscribe({
      next: (resposta) => {
        if (resposta.erro){
          this.cepNaoEncontrado = true
          this.cadastroForm.patchValue({endereco: {logradouro: '', 
                                  bairro: '', 
                                  cidade: '', 
                                  uf: ''}})
        }else{
          this.cadastroForm.patchValue({endereco: {logradouro: resposta.logradouro, 
                                  bairro: resposta.bairro, 
                                  cidade: resposta.localidade, 
                                  uf: resposta.uf}})
        }
      },
      error: (err) => {
        console.error(err)
        this.erroRequisicao = true
      } 
    });
    
  }

}
