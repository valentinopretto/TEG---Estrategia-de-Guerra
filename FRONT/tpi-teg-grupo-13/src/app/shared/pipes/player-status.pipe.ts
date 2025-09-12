import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'playerStatus'
})
export class PlayerStatusPipe implements PipeTransform {

  transform(value: unknown, ...args: unknown[]): unknown {
    return null;
  }

}
