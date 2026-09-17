import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

@Component({
  imports: [RouterOutlet, RouterLink],
  selector: 'app-cliente-layout',
  styleUrl: './cliente-layout.css',
  templateUrl: './cliente-layout.html',
})
export class ClienteLayout {}
