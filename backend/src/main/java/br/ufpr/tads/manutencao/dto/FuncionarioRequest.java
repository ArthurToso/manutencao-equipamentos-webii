package br.ufpr.tads.manutencao.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public class FuncionarioRequest {
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, message = "O nome deve conter no mínimo dois caracteres" )
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido") 
    private String email;
    
    @NotNull(message = "Data de Nascimento é obrigatória")
    @Past(message = "Data de Nascimento inválida") 
    private LocalDate dataNascimento;
    
    @Size(min = 8, message = "A senha precisa conter no mínimo 8 caracteres")
    private String senha;

    public FuncionarioRequest(){}

    public FuncionarioRequest(String nome, String email, LocalDate dataNascimento){
        this.nome = nome;
        this.email = email;
        this.dataNascimento = dataNascimento;
    }

    public String getNome(){
        return nome;
    }

    public void setNome(String nome){
        this.nome = nome;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public LocalDate getDataNascimento(){
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento){
        this.dataNascimento = dataNascimento;
    }

    public String getSenha(){
        return senha;
    }

    public void setSenha(String senha){
        this.senha = senha;
    }

}
