import { Component } from '@angular/core';

import {RouterOutlet} from '@angular/router';
import {GameMapComponent} from './features/game/components/game-map/game-map.component';


@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'TEG Online';
}
