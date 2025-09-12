// src/app/features/join-game/components/join-game.module.ts
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { JoinGameComponent } from './join-game.component';

@NgModule({
  declarations: [],
  imports: [CommonModule, FormsModule, JoinGameComponent],
  exports: [JoinGameComponent]
})
export class JoinGameModule {}
