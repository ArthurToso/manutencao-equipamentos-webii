import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-funcionario-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './funcionario-layout.html',
  styleUrl: './funcionario-layout.css'
})
export class FuncionarioLayout {}
