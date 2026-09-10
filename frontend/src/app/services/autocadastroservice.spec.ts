import { TestBed } from '@angular/core/testing';
import { AutocadastroService } from './autocadastroservice';

describe('Autocadastroservice', () => {
  let service: AutocadastroService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AutocadastroService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
