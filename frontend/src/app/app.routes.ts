import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Autocadastro } from './pages/autocadastro/autocadastro';
import { ClienteHome } from './pages/cliente/cliente-home/cliente-home';
import { ClienteNovaSolicitacao } from './pages/cliente/cliente-nova-solicitacao/cliente-nova-solicitacao';
import { ClienteLayout } from './pages/cliente/cliente-layout/cliente-layout'; // Ajuste o caminho se necessário

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'cadastro', component: Autocadastro },
  
  // Rota Pai que carrega o Layout (Header + Fundo)
  { 
    path: 'cliente', 
    component: ClienteLayout, 
    children: [
      { path: 'home', component: ClienteHome },
      { path: 'nova-solicitacao', component: ClienteNovaSolicitacao },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },
  
  { path: 'cliente/**', redirectTo: 'cliente/home' },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];