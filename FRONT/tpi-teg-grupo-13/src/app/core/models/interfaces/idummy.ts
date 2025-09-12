import {DummyType} from '../../enums/dummy-type';

export interface IDummy {
  id: number;
  name: string;
  type: DummyType;
  isActive: boolean;
}
