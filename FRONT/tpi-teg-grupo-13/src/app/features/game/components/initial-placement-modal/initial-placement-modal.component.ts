import { Component, EventEmitter, Input, Output } from '@angular/core';
import { GamePlayService } from '../../services/game.play.service';
import { CommonModule } from '@angular/common';

interface CountryPlacement {
  id: number;
  name: string;
  currentArmies: number;
  allocated: number;
}

@Component({
  selector: 'app-initial-placement-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './initial-placement-modal.component.html',
  styleUrls: ['./initial-placement-modal.component.css']
})
export class InitialPlacementModalComponent {
  @Input() gameCode!: string;
  @Input() playerId!: number;
  @Input() mode!: 'initial' | 'normal';
  @Input() availableArmies!: number;
  @Input() countries: CountryPlacement[] = [];
  @Output() closed = new EventEmitter<void>();
  @Output() placed = new EventEmitter<void>();

  constructor(private gamePlay: GamePlayService) {}

  inc(country: CountryPlacement) {
    if (this.availableArmies > 0) {
      country.allocated++;
      this.availableArmies--;
    }
  }

  dec(country: CountryPlacement) {
    if (country.allocated > 0) {
      country.allocated--;
      this.availableArmies++;
    }
  }

  submit() {
    const armiesByCountry: { [id: number]: number } = {};

    this.countries.forEach(c => {
      if (c.allocated > 0) {
        armiesByCountry[c.id] = c.allocated;
      }
    });

    if (this.mode === 'initial') {
      this.gamePlay.placeInitialArmies(this.gameCode, {
        playerId: this.playerId,
        armiesByCountry
      }).subscribe({
        next: () => {
          this.placed.emit();
          this.closed.emit();
        },
        error: (error) => {
          console.error('Error placing initial armies:', error);
        }
      });
    } else {
      this.gamePlay.placeReinforcementArmies(this.gameCode, {
        playerId: this.playerId,
        armiesByCountry
      }).subscribe({
        next: () => {
          this.placed.emit();
          this.closed.emit();
        },
        error: (error) => {
          console.error('Error placing reinforcement armies:', error);
        }
      });
    }
  }

  close() {
    this.closed.emit();
  }
}
