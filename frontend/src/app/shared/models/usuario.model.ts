import { Endereco } from "./endereco.model";

export class Usuario {

    constructor(
        public id: number = 0,
        public CPF: string = '',
        public nome: string = '',
        public email: string = '',
        public endereco: number = 0,
        public telefone: string = ''
    ){}
    
}
