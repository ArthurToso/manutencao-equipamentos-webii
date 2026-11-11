import { Injectable } from '@angular/core';
import { NgbDateParserFormatter, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';

@Injectable()
export class DataBrParserFormatter extends NgbDateParserFormatter {

  parse(value: string): NgbDateStruct | null {
    const partes = value?.trim().split('/');
    if (!partes || partes.length !== 3) {
      return null;
    }
    const [dia, mes, ano] = partes.map(Number);
    if (!dia || !mes || !ano) {
      return null;
    }
    return { day: dia, month: mes, year: ano };
  }

  format(date: NgbDateStruct | null): string {
    if (!date) {
      return '';
    }
    const dia = String(date.day).padStart(2, '0');
    const mes = String(date.month).padStart(2, '0');
    return `${dia}/${mes}/${date.year}`;
  }
}