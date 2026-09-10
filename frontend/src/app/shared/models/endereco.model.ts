export class Endereco {
    
    constructor(
        public id: number = 0,
        public CEP: string = '',
        public logradouro: string = '',
        public numero: number = 0,
        public bairro: string = '',
        public cidade: string = '',
        public uf: string = ''
    ){

    }

}
