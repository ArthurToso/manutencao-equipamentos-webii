import { EstadoUF } from "../enums";

export interface Endereco {
    cep: string;
    logradouro: string;
    numero: number;
    complemento?: string;
    bairro: string;
    cidade: string;
    estado: EstadoUF;   
}
