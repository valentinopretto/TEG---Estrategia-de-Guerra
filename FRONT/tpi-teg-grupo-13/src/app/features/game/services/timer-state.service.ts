import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TimerStateService {
  private readonly TIMER_KEY = 'teg_timer_state';
  private stateSubject = new BehaviorSubject<TimerState | null>(null);

  constructor() {
    this.loadInitialState();
    window.addEventListener('storage', this.handleStorageEvent.bind(this));
  }

  private loadInitialState() {
    const saved = localStorage.getItem(this.TIMER_KEY);
    if (saved) {
      try {
        const state = JSON.parse(saved);
        this.stateSubject.next(state);
      } catch {
        localStorage.removeItem(this.TIMER_KEY);
      }
    }
  }

  private handleStorageEvent(event: StorageEvent) {
    if (event.key === this.TIMER_KEY) {
      if (event.newValue) {
        this.stateSubject.next(JSON.parse(event.newValue));
      } else {
        this.stateSubject.next(null);
      }
    }
  }

  updateState(gameCode: string, playerIndex: number, duration: number) {
    const newState: TimerState = {
      gameCode,
      playerIndex,
      startTime: Date.now(),
      initialDuration: duration
    };

    localStorage.setItem(this.TIMER_KEY, JSON.stringify(newState));
    this.stateSubject.next(newState);
  }

  clearState() {
    localStorage.removeItem(this.TIMER_KEY);
    this.stateSubject.next(null);
  }

  get currentState$() {
    return this.stateSubject.asObservable();
  }
}

interface TimerState {
  gameCode: string;
  playerIndex: number;
  startTime: number; // timestamp
  initialDuration: number; // segundos
}
