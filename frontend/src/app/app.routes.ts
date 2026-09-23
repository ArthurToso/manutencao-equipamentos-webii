import { Routes } from '@angular/router';
import {
  Login, Autocadastro,
  ClienteLayout, ClienteHome, ClienteNovaSolicitacao, ClienteOrcamento, ClienteSolicitacao,
  FuncionarioLayout, FuncionarioHome, FuncionarioOrcamento, FuncionarioSolicitacoes, FuncionarioManutencao, 
  FuncionarioCategorias, FuncionarioCategoriasEditarNovo, ClientePagarServico,
  FuncionarioRelatorioReceitas, FuncionarioRelatorioCategorias
} from './pages';

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
      { path: 'pagamento/:id', component: ClientePagarServico},
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: '**', redirectTo: 'home' },
    ]},
  { path: 'funcionario',
    component: FuncionarioLayout,
    children: [
      { path: 'home', component: FuncionarioHome },
      { path: 'orcamento/:id', component: FuncionarioOrcamento },
      { path: 'solicitacoes', component: FuncionarioSolicitacoes },
      { path: 'manutencao/:id', component: FuncionarioManutencao },
      { path: 'categorias', component:FuncionarioCategorias },
      { path: 'categorias/inserir', component:FuncionarioCategoriasEditarNovo },
      { path: 'categorias/editar/:id', component:FuncionarioCategoriasEditarNovo },
      { path: 'relatorio-receitas', component: FuncionarioRelatorioReceitas },
      { path: 'relatorio-categoria', component: FuncionarioRelatorioCategorias },
      { path: '', redirectTo: 'home', pathMatch: 'full' },
      { path: '**', redirectTo: 'home' },
    ]},
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];