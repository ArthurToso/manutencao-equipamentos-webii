import { Routes } from '@angular/router';
import { Login } from './pages/login/login';
import { Autocadastro } from './pages/autocadastro/autocadastro';
import { ClienteHome } from './pages/cliente/cliente-home/cliente-home';
import { ClienteNovaSolicitacao } from './pages/cliente/cliente-nova-solicitacao/cliente-nova-solicitacao';
import { ClienteOrcamento } from './pages/cliente/cliente-orcamento/cliente-orcamento';
import { ClienteLayout } from './pages/cliente/cliente-layout/cliente-layout'; // Ajuste o caminho se necessário
import { ClienteSolicitacao } from './pages/cliente/cliente-solicitacao/cliente-solicitacao';
import { FuncionarioLayout } from './pages/funcionario/funcionario-layout/funcionario-layout';
import { FuncionarioHome } from './pages/funcionario/funcionario-home/funcionario-home';
import { FuncionarioOrcamento } from './pages/funcionario/funcionario-orcamento/funcionario-orcamento';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'cadastro', component: Autocadastro },
  { path: 'cliente', 
    component: ClienteLayout, 
    children: [
      { path: 'home', component: ClienteHome },
      { path: 'nova-solicitacao', component: ClienteNovaSolicitacao },
      { path: 'orcamento/:id', component: ClienteOrcamento },
      { path: 'solicitacao/:id', component: ClienteSolicitacao},
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: '**', redirectTo: 'home' },
    ]},
  { path: 'funcionario',
    component: FuncionarioLayout,
    children: [
      { path: 'home', component: FuncionarioHome },
      { path: 'orcamento/:id', component: FuncionarioOrcamento },
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: '**', redirectTo: 'home' },
    ]},
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];