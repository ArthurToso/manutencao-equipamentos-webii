import { Endereco } from "./endereco.model";

export interface UsuarioDTO {
    id?: number;
    nome: string;
    email: string;
    perfil?: 'CLIENTE' | 'FUNCIONARIO';
}

export interface ClienteDTO extends UsuarioDTO {
    telefone: string;
    cpf: string;
    endereco: Endereco;
}

export interface FuncionarioDTO extends UsuarioDTO {
    dtNasc: string;
}

//funcionario precisa passar a senha ao back
export interface FuncionarioCrudDTO extends FuncionarioDTO {
    senha: string;
}
