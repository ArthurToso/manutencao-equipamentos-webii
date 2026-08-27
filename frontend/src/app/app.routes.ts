import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Autocadastro } from './pages/autocadastro/autocadastro';
import { ClienteHome } from './pages/cliente-home/cliente-home';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'cadastro', component: Autocadastro },
  { path: 'cliente/home', component: ClienteHome }
];