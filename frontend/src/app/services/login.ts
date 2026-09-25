import { Injectable, Service } from '@angular/core';
import { Observable, of } from 'rxjs';
import { UsuarioDTO, LoginDTO } from '../shared';


const LS_CHAVE: string = "userLogado";

//refatorar
@Injectable({
    providedIn: 'root'
})
export class LoginService {
    public get usuarioLogado(): UsuarioDTO | null {
    let usu = localStorage[LS_CHAVE];
        return (usu ? JSON.parse(localStorage[LS_CHAVE]) : null);
    }
    public set usuarioLogado(usuario: UsuarioDTO) {
        localStorage[LS_CHAVE] = JSON.stringify(usuario);
    }
    logout() {
       delete localStorage[LS_CHAVE];
    }

    login(login: LoginDTO): Observable<UsuarioDTO | null> {
        //substituir por consulta a API e leitura do token JWT
        
        let usu : UsuarioDTO = {
            nome : login.login,
            email : login.login
        };


        if (login.login == login.senha) {
            if (login.login == "funcionario") {
                usu.perfil = "FUNCIONARIO";
            }
            else if (login.login == "cliente") {
                usu.perfil = "CLIENTE";
            }
            return of(usu);
        }
        else {
         return of(null);
        }
    }
}