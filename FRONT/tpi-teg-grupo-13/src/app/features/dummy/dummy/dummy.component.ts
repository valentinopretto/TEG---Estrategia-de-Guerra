import { Component } from '@angular/core';
import {IDummy} from '../../../core/models/interfaces/idummy';
import {DummyService} from '../../../core/services/dummy.service';
import {DummyType} from '../../../core/enums/dummy-type';
import {DummyFormatterPipe} from '../../../shared/pipes/dummy-formatter.pipe';
import {HighlightDummyDirective} from '../../../shared/directives/highlight-dummy.directive';
import {NgClass} from '@angular/common';
import {Dummy} from '../../../core/models/class/dummy';

@Component({
  selector: 'app-dummy',
  imports: [
    DummyFormatterPipe,
    HighlightDummyDirective,
    NgClass
  ],
  templateUrl: './dummy.component.html',
  styleUrl: './dummy.component.css'
})
export class DummyComponent {
  dummies: IDummy[] = [];

  constructor(private dummyService: DummyService) {}

  ngOnInit(): void {
    this.dummies = [
      this.dummyService.createDummy(1, 'Dummy Básico', DummyType.BASIC, true),
      this.dummyService.createDummy(2, 'Dummy Avanzado', DummyType.ADVANCED, false),
      this.dummyService.createDummy(3, 'Dummy Premium', DummyType.PREMIUM, true),
    ];
  }
  getDummyGreeting(id:number,name:string): string {
    const dummy = new Dummy(id, name);
    return dummy.sayHello();
  }

  toggleStatus(dummy: IDummy): void {
    const updatedDummy = this.dummyService.toggleDummyStatus(dummy);
    this.dummies = this.dummies.map((d) => (d.id === dummy.id ? updatedDummy : d));
  }
}
