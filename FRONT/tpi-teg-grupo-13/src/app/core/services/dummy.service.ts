import { Injectable } from '@angular/core';
import { Dummy } from '../models/class/dummy';
import { IDummy } from '../models/interfaces/idummy';
import { DummyType } from '../enums/dummy-type';

@Injectable({
  providedIn: 'root',
})
export class DummyService {
  constructor() {}

  // Método para crear una instancia de Dummy y convertirla a IDummy
  createDummy(id: number, name: string, type: DummyType, isActive: boolean): IDummy {
    const dummy = new Dummy(id, name);
    return {
      id: dummy.id,
      name: dummy.name,
      type: type,
      isActive: isActive,
    };
  }

  // Método para filtrar una lista de IDummy por tipo
  filterDummiesByType(dummies: IDummy[], type: DummyType): IDummy[] {
    return dummies.filter((dummy) => dummy.type === type);
  }

  // Método para activar o desactivar un IDummy
  toggleDummyStatus(dummy: IDummy): IDummy {
    return { ...dummy, isActive: !dummy.isActive };
  }
}
