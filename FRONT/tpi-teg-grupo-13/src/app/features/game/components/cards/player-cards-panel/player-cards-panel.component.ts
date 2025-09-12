import {Component, Input, numberAttribute, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {interval, Subject, takeUntil} from 'rxjs';
import {CardDisplayComponent} from '../card-display/card-display.component';
import {TerritoryCard} from '../../../../../core/models/interfaces/card';
import {CardService} from '../../../services/card.service';
import {NotificationService} from '../../../../../core/services/notification.service';
import {CardTradeRequest} from '../../../../../core/models/interfaces/card-trade-request';
import {CardType} from '../../../../../core/enums/card-type';
import {CardSymbol} from '../../../../../core/enums/card-symbol';


@Component({
  standalone: true,
  selector: 'app-player-cards-panel',
  imports: [ CommonModule, CardDisplayComponent
  ],
  templateUrl: './player-cards-panel.component.html',
  styleUrls: ['./player-cards-panel.component.css']
})

export class PlayerCardsPanelComponent implements OnInit, OnDestroy {
  @Input({transform: numberAttribute}) playerId!: number;

  playerCards: TerritoryCard[] = [];
  selectedCards: TerritoryCard[] = [];
  canTrade: boolean = false;
  mustTrade: boolean = false;
  maxCards: number = 5;
  isTrading: boolean = false;
  canSelectCards: boolean = true;

  private destroy$ = new Subject<void>();
  feedbackMessage: string = '';
  constructor(
    private cardService: CardService,
    private notificationService: NotificationService
  ) {}

  debugMode: boolean = true; // Cambiá esto a false para usar backend

  ngOnInit() {
    if (this.debugMode) {
    this.playerCards = [
      {
        id: 1,
        symbol: CardSymbol.BALLOON,
        type: CardType.INFANTRY,
        territory: 'Argentina',
        isWildcard: false
      },
      {
        id: 2,
        symbol: CardSymbol.CANNON,
        type: CardType.CANNON,
        territory: 'Chile',
        isWildcard: false
      },
      {
        id: 3,
        symbol: CardSymbol.SHIP,
        type: CardType.CAVALRY,
        territory: 'México',
        isWildcard: false
      }
    ];
      this.maxCards = 5;
      this.selectedCards = [];
      this.canSelectCards = true;
      this.updateFeedbackMessage();
    } else {
      this.loadMaxCards();
      this.loadPlayerCards();

      interval(10000)
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => {
          this.loadPlayerCards();
        });
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadMaxCards() {
    this.cardService.getMaxCardsAllowed()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (max) => this.maxCards = max,
        error: (error) => console.error('Error loading max cards:', error)
      });
  }

  loadPlayerCards() {
    if (!this.playerId) return;

    this.cardService.getPlayerCards(this.playerId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (cards) => {
          this.playerCards = cards;
          this.checkTradeStatus();
        },
        error: (error) => {
          console.error('Error loading player cards:', error);
          this.notificationService.showNotification('error', 'Error', 'No se pudieron cargar las cartas');
        }
      });
  }

  private checkTradeStatus() {
    // Verificar si debe canjear obligatoriamente
    this.cardService.mustPlayerTrade(this.playerId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (must) => {
          this.mustTrade = must;

          if (must) {
            this.canSelectCards = true;
            this.canTrade = true; // Forzar canje
            this.notificationService.showNotification(
              'warning',
              '¡Canje Obligatorio!',
              'Tienes 5 o más cartas. Debes canjear antes de continuar.'
            );
          } else {
            // Verificar si puede canjear
            this.cardService.canPlayerTrade(this.playerId)
              .pipe(takeUntil(this.destroy$))
              .subscribe({
                next: (can) => {
                  this.canTrade = can && this.isValidTradeSelection();
                }
              });
          }
        }
      });
  }

  onCardSelected(TerritoryCard: TerritoryCard) {
    if (!this.canSelectCards) return;

    const index = this.selectedCards.findIndex(c => c.id === TerritoryCard.id);

    if (index >= 0) {
      // Deseleccionar
      this.selectedCards.splice(index, 1);
    } else {
      // Seleccionar (máximo 3)
      if (this.selectedCards.length < 3) {
        this.selectedCards.push(TerritoryCard);
      }
    }

    this.updateTradeStatus();
  }

  isCardSelected(TerritoryCard: TerritoryCard): boolean {
    return this.selectedCards.some(c => c.id === TerritoryCard.id);
  }

  private updateTradeStatus() {
    if (this.mustTrade) {
      this.canTrade = this.selectedCards.length === 3;
    } else {
      this.canTrade = this.isValidTradeSelection();
    }
    this.updateFeedbackMessage();
  }

  private isValidTradeSelection(): boolean {
    if (this.selectedCards.length !== 3) return false;

    // Verificar combinaciones válidas
    const types = this.selectedCards.map(c => c.type);
    const hasWildcard = types.includes(CardType.WILDCARD);

    if (hasWildcard) return true;

    // Todas iguales
    const allSame = types.every(t => t === types[0]);
    if (allSame) return true;

    // Todas diferentes
    const uniqueTypes = new Set(types);
    return uniqueTypes.size === 3;
  }

  attemptTrade() {
    if (!this.canTrade || this.isTrading) return;

    if (this.selectedCards.length !== 3) {
      this.notificationService.showNotification('warning', 'Atención', 'Debes seleccionar exactamente 3 cartas');
      return;
    }

    this.isTrading = true;

    const tradeRequest: CardTradeRequest = {
      playerId: this.playerId,
      cardIds: this.selectedCards.map(c => c.id)
    };

    this.cardService.tradeCards(tradeRequest)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (troopsGained) => {
          this.notificationService.showNotification(
            'success',
            '¡Canje Exitoso!',
            `Ganaste ${troopsGained} tropas`
          );

          this.selectedCards = [];
          this.loadPlayerCards(); // Recargar cartas
          this.isTrading = false;
        },
        error: (error) => {
          console.error('Error trading cards:', error);
          this.notificationService.showNotification('error', 'Error', 'No se pudo realizar el canje');
          this.isTrading = false;
        }
      });
  }
  private showTradeSuccess(troopsGained: number) {
    this.notificationService.showNotification(
      'success',
      '¡Canje exitoso!',
      `Ganaste ${troopsGained} tropas`
    );
  }
  private updateFeedbackMessage() {
    if (this.playerCards.length === 0) {
      this.feedbackMessage = 'No tienes cartas';
    } else if (this.mustTrade) {
      this.feedbackMessage = '¡Debes canjear cartas obligatoriamente!';
    } else if (this.selectedCards.length === 0) {
      this.feedbackMessage = 'Selecciona 3 cartas para canjear';
    } else if (this.selectedCards.length < 3) {
      this.feedbackMessage = `Selecciona ${3 - this.selectedCards.length} carta(s) más`;
    } else {
      this.feedbackMessage = 'Combinación válida - ¡Puedes canjear!';
    }

  }
}
