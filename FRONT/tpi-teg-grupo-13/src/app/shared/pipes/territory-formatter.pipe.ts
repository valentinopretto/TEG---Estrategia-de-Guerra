import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'territoryFormatter'
})
export class TerritoryFormatterPipe implements PipeTransform {
  transform(value: string): string {
    return value.replace(/-/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
  }
}
