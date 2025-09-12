// src/app/features/game/components/initial-placement/troop-counter.component.ts
import {
  Component,
  Input,
  AfterViewInit,
  ElementRef,
  ViewChild,
  NO_ERRORS_SCHEMA
} from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-troop-counter',
  standalone: true,
  imports:    [ CommonModule ],        // para *ngIf, ngFor… si lo necesitas
  schemas:    [ NO_ERRORS_SCHEMA ],    // permite etiquetas SVG arbitrarias
  templateUrl:'./troop-counter.component.html',
  styleUrls:  ['./troop-counter.component.css']
})
export class TroopCounterComponent implements AfterViewInit {
  @Input() troop!: { id: string; path: string; troops: number };

  @ViewChild('circlePath', { static: true })
  pathRef!: ElementRef<SVGPathElement>;

  centerX = 0;
  centerY = 0;

  ngAfterViewInit() {
    const bbox = this.pathRef.nativeElement.getBBox();
    this.centerX = bbox.x + bbox.width  / 2;
    this.centerY = bbox.y + bbox.height / 2;
  }
}
