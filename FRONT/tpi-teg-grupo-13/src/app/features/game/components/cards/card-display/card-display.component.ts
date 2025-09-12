import {Component, EventEmitter, Input, Output} from '@angular/core';
import { NgClass } from '@angular/common';
import {CardType} from '../../../../../core/enums/card-type';
import {CardSymbol} from '../../../../../core/enums/card-symbol';
import {TerritoryCard} from '../../../../../core/models/interfaces/card';

@Component({
  selector: 'app-card-display',
  standalone: true,
  imports: [NgClass],
  templateUrl: './card-display.component.html',
  styleUrl: './card-display.component.css'
})

export class CardDisplayComponent {
  @Input() card!: TerritoryCard;
  @Input() isSelected: boolean = false;
  @Input() canSelect: boolean = true;
  @Output() cardSelected = new EventEmitter<TerritoryCard>();

  onCardClick() {
    if (this.canSelect) {
      this.cardSelected.emit(this.card);
    }
  }

  getSymbolIcon(): string {
    switch (this.card.symbol) {
      case CardSymbol.BALLOON: return '🎈';
      case CardSymbol.CANNON: return '💣';
      case CardSymbol.SHIP: return '⛵';
      case CardSymbol.WILDCARD: return '⭐';
      default: return '❓';
    }
  }

  getMainIcon(): string {
    switch (this.card.type) {
      case CardType.INFANTRY: return '🚶';
      case CardType.CAVALRY: return '🐎';
      case CardType.CANNON: return '🔫';
      case CardType.WILDCARD: return '⭐';
      default: return '❓';
    }
  }

  getIconColorClass(): string {
    switch (this.card.type) {
      case CardType.INFANTRY: return 'text-green-600';
      case CardType.CAVALRY: return 'text-blue-600';
      case CardType.CANNON: return 'text-red-600';
      case CardType.WILDCARD: return 'text-purple-600';
      default: return 'text-gray-600';
    }
  }
  getCardTypeClass(): string {
    return {
      [CardType.INFANTRY]: 'infantry',
      [CardType.CAVALRY]: 'cavalry',
      [CardType.CANNON]: 'cannon',
      [CardType.WILDCARD]: 'wildcard'
    }[this.card.type] || '';
  }

  getCardTypeName(): string {
    switch (this.card.type) {
      case CardType.INFANTRY: return 'INF';
      case CardType.CAVALRY: return 'CAB';
      case CardType.CANNON: return 'CAN';
      case CardType.WILDCARD: return 'COM';
      default: return '???';
    }
  }
}
