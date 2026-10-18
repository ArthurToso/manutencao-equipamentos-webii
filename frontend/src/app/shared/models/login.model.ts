import { UsuarioDTO } from './usuario.model';

export interface LoginDTO {
  email: string;
  senha: string;
}

export interface LoginResponseDTO {
  token: string;
  tipo: string;
  expiraEm: number | string;
  usuario: UsuarioDTO;
}
