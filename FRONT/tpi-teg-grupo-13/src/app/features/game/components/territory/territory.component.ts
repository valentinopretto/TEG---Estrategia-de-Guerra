import { Component, Input, Output, EventEmitter } from '@angular/core';
import { NO_ERRORS_SCHEMA } from '@angular/core';
@Component({
  selector: 'app-territory',
  templateUrl: './territory.component.html',
  styleUrls: ['./territory.component.css'],
  schemas: [NO_ERRORS_SCHEMA]
})
export class TerritoryComponent {
  @Input() territory!: { id: string; name: string; path: string };
  @Input() fillColor: string = '#a67c52';
  @Input() strokeColor: string = '#000';
  @Input() strokeWidth: string = '1.5';
  @Output() selectTerritory = new EventEmitter<string>();


  onClick() {
    this.selectTerritory.emit(this.territory.id);
  }
}
