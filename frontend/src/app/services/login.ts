import { Observable, of } from 'rxjs';
import { computed, Service, signal } from '@angular/core';
import { UsuarioDTO, LoginDTO } from '../shared';

const MOCK_USERS: any [] = [
    {
        id: 1,
        nome: 'John Doe',
        email: 'john.doe@gmail.com',
        perfil: 'CLIENTE'
    },
    {
        id: 2,
        nome: 'Maria Doe',
        email: 'maria.doe@gmail.com',
        perfil: 'FUNCIONARIO'
    }
]

@Service()
export class Login {

    curUser = signal<UsuarioDTO | null>(null);

    isLoggedIn = computed(() => !!this.curUser());
    userRole = computed(() => this.curUser()?.perfil);

    login(login: LoginDTO): Observable<UsuarioDTO | null>{
        //sera o request ao api/login
        let user = MOCK_USERS.find( u => u.email === login.login && u.senha === login.senha);

        if(user){
            this.curUser.set(user);
            return of(user);
        }else{
            return of(null);
        }

    }

    logout(): void {
        this.curUser.set(null);
    }


}
