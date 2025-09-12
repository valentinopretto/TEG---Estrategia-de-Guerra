import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  OnDestroy,
  SimpleChanges
} from '@angular/core';
import { interval, Subscription } from 'rxjs';

interface TurnTimerState {
  gameCode: string;
  playerIndex: any;
  startTime: number;
  duration: number;
}

@Component({
  selector: 'app-turn-timer',
  template: `<div class="timer" [class.warning]="remaining <= 10">{{ display }}</div>`,
  styles: [`
    .timer {
      font-family: monospace;
      font-size: 1.2rem;
      font-weight: bold;
      color: #fff;
    }
    .timer.warning {
      color: #ef4444;
      animation: pulse 1s infinite;
    }
    @keyframes pulse {
      0%,100% { opacity: 1; }
      50% { opacity: 0.5; }
    }
  `]
})
export class TurnTimerComponent implements OnChanges, OnDestroy {
  @Input() duration: string | number = 0;    // segundos o "mm:ss"
  @Input() resetTrigger!: any;               // currentPlayerIndex
  @Input() gameCode!: string;                // código de la partida
  @Output() timeUp = new EventEmitter<void>();

  display   = '00:00';
  remaining = 0;

  private sub: Subscription | null = null;
  private actualDuration = 0;
  private userId: string | number = '';
  private prevGameCode: string | null = null;
  private prevTrigger: any = undefined;
  private readonly BASE_KEY = 'turn_timer_state';

  constructor() {
    // Extraemos userId para namespacing
    const raw = localStorage.getItem('teg_current_user');
    if (raw) {
      try { this.userId = JSON.parse(raw).id; } catch {}
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    // 1) Cada vez obtenemos la duración
    this.actualDuration = this.parseDuration(this.duration);

    // 2) Si cambia de partida, borramos el estado de la anterior
    if (changes['gameCode'] && !changes['gameCode'].firstChange) {
      const oldCode = changes['gameCode'].previousValue as string;
      const oldKey = this.buildKey(oldCode, this.prevTrigger);
      localStorage.removeItem(oldKey);
      // Reseteamos prevTrigger para forzar nueva inicialización
      this.prevTrigger = undefined;
    }
    this.prevGameCode = this.gameCode;

    // 3) Cuando resetTrigger cambia por primera vez EN ESTE TURNO
    if (
      changes['resetTrigger'] &&
      (
        this.prevTrigger === undefined ||
        changes['resetTrigger'].currentValue !== changes['resetTrigger'].previousValue
      )
    ) {
      this.prevTrigger = changes['resetTrigger'].currentValue;
      // Si ya tenía guardado un estado válido, lo continuamos
      if (this.hasSavedState()) {
        this.continueTurn();
      } else {
        // Si no, arrancamos un turno nuevo con full duración
        this.startNewTurn();
      }
    }
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }

  // —————————————————————————————————————
  // Construye la clave para guardar/leer en localStorage
  // —————————————————————————————————————
  private buildKey(code: string|null, trigger: any): string {
    return [
      this.BASE_KEY,
      code ?? '',
      trigger ?? '',
      this.userId
    ].join('_');
  }

  private get storageKey(): string {
    return this.buildKey(this.gameCode, this.resetTrigger);
  }

  // —————————————————————————————————————
  // ¿Hay un estado válido guardado?
  // —————————————————————————————————————
  private hasSavedState(): boolean {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) return false;
    try {
      const s = JSON.parse(raw) as TurnTimerState;
      if (s.gameCode !== this.gameCode || s.playerIndex !== this.resetTrigger) {
        return false;
      }
      const elapsed = (Date.now() - s.startTime) / 1000;
      return s.duration === this.actualDuration && elapsed < s.duration;
    } catch {
      return false;
    }
  }

  // —————————————————————————————————————
  // Iniciar un nuevo turno (guardo estado + arranco full)
  // —————————————————————————————————————
  private startNewTurn() {
    this.sub?.unsubscribe();
    const now = Date.now();
    const state: TurnTimerState = {
      gameCode: this.gameCode,
      playerIndex: this.resetTrigger,
      startTime: now,
      duration: this.actualDuration
    };
    localStorage.setItem(this.storageKey, JSON.stringify(state));
    this.remaining = this.actualDuration;
    this.startCountdown();
  }

  // —————————————————————————————————————
  // Continuar un turno ya iniciado (recupero remanente)
  // —————————————————————————————————————
  private continueTurn() {
    this.sub?.unsubscribe();
    const raw = localStorage.getItem(this.storageKey)!;
    const s = JSON.parse(raw) as TurnTimerState;
    const elapsed = Math.floor((Date.now() - s.startTime) / 1000);
    this.remaining = Math.max(0, s.duration - elapsed);
    this.startCountdown();
  }

  // —————————————————————————————————————
  // Borrar el estado al terminar
  // —————————————————————————————————————
  private cleanup() {
    localStorage.removeItem(this.storageKey);
  }

  // —————————————————————————————————————
  // Countdown
  // —————————————————————————————————————
  private startCountdown() {
    this.updateDisplay(this.remaining);

    if (this.remaining <= 0) {
      this.timeUp.emit();
      return this.cleanup();
    }

    this.sub = interval(1000).subscribe(() => {
      this.remaining = Math.max(0, this.remaining - 1);
      this.updateDisplay(this.remaining);

      // Cada 5s re-sincronizo el localStorage
      if (this.remaining % 5 === 0) {
        const now = Date.now();
        const partial: TurnTimerState = {
          gameCode: this.gameCode,
          playerIndex: this.resetTrigger,
          startTime: now - ((this.actualDuration - this.remaining) * 1000),
          duration: this.actualDuration
        };
        localStorage.setItem(this.storageKey, JSON.stringify(partial));
      }

      if (this.remaining <= 0) {
        this.sub!.unsubscribe();
        this.cleanup();
        this.timeUp.emit();
      }
    });
  }

  // —————————————————————————————————————
  // Formato MM:SS
  // —————————————————————————————————————
  private updateDisplay(sec: number) {
    const m = String(Math.floor(sec / 60)).padStart(2, '0');
    const s = String(sec % 60).padStart(2, '0');
    this.display = `${m}:${s}`;
  }

  // —————————————————————————————————————
  // Parsear distintos formatos de duration
  // —————————————————————————————————————
  private parseDuration(d: string | number): number {
    if (typeof d === 'number') return Math.floor(d);
    const n = parseInt(d as string, 10);
    if (!isNaN(n)) return n;
    if ((d as string).includes(':')) {
      const [m, s] = (d as string).split(':').map(x => parseInt(x, 10));
      if (!isNaN(m) && !isNaN(s)) return m * 60 + s;
    }
    return 0;
  }
}
