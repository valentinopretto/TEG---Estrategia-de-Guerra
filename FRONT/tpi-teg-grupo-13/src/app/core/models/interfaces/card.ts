import { CardSymbol } from "../../enums/card-symbol";
import {CardType} from '../../enums/card-type';

export interface TerritoryCard {
  id: number;
  symbol: CardSymbol;
  type: CardType;
  territory?: string;
  isWildcard: boolean;
}
