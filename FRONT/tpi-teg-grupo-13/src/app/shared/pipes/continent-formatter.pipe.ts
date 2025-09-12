import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'continentFormatter'
})
export class ContinentFormatterPipe implements PipeTransform {
  transform(value: string): string {
    const map: Record<string, string> = {
      'north-america': 'Norteamérica',
      'south-america': 'Sudamérica',
      'europe': 'Europa',
      'asia': 'Asia',
      'africa': 'África',
      'oceania': 'Oceanía'
    };
    return map[value.toLowerCase()] || value;
  }
}
