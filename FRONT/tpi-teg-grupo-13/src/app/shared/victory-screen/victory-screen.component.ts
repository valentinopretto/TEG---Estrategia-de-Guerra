import { AfterViewInit, Component, Input, Output, ElementRef, EventEmitter , Renderer2, ViewChild} from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-victory-screen',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './victory-screen.component.html',
  styleUrl: './victory-screen.component.css'
})
export class VictoryScreenComponent implements AfterViewInit {
  @Input() isVisible: boolean = false;
  @Input() winnerName: string = '';
  @Input() showTrophyImage: boolean = true;
  @Input() showStats: boolean = true;
  @Input() showPlayAgainButton: boolean = false;
  @Input() totalPlayers: number = 0;
  @Input() gameDuration: string = '';
  @Input() totalTurns: number = 0;

  @Output() backToLobby = new EventEmitter<void>();
  @Output() playAgain = new EventEmitter<void>();

  @ViewChild('confettiContainer', { static: false }) confettiContainer!: ElementRef;

  constructor(private renderer: Renderer2) {
  }
  ngAfterViewInit(): void {
    this.createConfetti();
    this.setupButtonClickEffects();
  }


  onBackToLobby(): void {
    this.backToLobby.emit();
  }

  onPlayAgain(): void {
    this.playAgain.emit();
  }

  createConfetti(): void {
    const colors = ['#ffd700', '#ff6b6b', '#4ecdc4', '#45b7d1', '#96ceb4', '#feca57'];

    for (let i = 0; i < 50; i++) {
      const confetti = this.renderer.createElement('div');
      this.renderer.addClass(confetti, 'confetti-piece');
      this.renderer.setStyle(confetti, 'left', `${Math.random() * 100}%`);
      this.renderer.setStyle(confetti, 'backgroundColor', colors[Math.floor(Math.random() * colors.length)]);
      this.renderer.setStyle(confetti, 'animationDelay', `${Math.random() * 4}s`);
      this.renderer.setStyle(confetti, 'animationDuration', `${(Math.random() * 3 + 3)}s`);
      this.renderer.appendChild(this.confettiContainer.nativeElement, confetti);
    }
  }

  setupButtonClickEffects(): void {
    const buttons = document.querySelectorAll('.action-btn');
    buttons.forEach(btn => {
      btn.addEventListener('click', () => {
        (btn as HTMLElement).style.transform = 'scale(0.95)';
        setTimeout(() => {
          (btn as HTMLElement).style.transform = '';
        }, 150);
      });
    });
  }

}

