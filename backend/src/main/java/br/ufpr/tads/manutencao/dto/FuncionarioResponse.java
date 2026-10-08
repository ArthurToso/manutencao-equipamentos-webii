package br.ufpr.tads.manutencao.dto;

import java.time.LocalDate;

public class FuncionarioResponse {
    private Long id;
    private String nome;
    private String email;
    private LocalDate dataNascimento;

    public FuncionarioResponse(){}

    public FuncionarioResponse(Long id, String nome, String email, LocalDate dataNascimento){
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.dataNascimento = dataNascimento;
    }

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
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

}
