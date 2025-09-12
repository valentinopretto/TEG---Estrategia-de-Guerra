import { Pipe, PipeTransform } from '@angular/core';
import {DummyType} from '../../core/enums/dummy-type';

@Pipe({
  name: 'dummyFormatter',
})
export class DummyFormatterPipe implements PipeTransform {
  transform(name: string, type: DummyType): string {
    switch (type) {
      case DummyType.BASIC:
        return `🔹 ${name}`;
      case DummyType.ADVANCED:
        return `🔸 ${name}`;
      case DummyType.PREMIUM:
        return `⭐ ${name}`;
      default:
        return name;
    }
  }
}
