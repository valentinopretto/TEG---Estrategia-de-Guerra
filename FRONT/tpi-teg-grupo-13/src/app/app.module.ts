import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { JoinGameModule } from './features/join-game/components/join-game.module';

@NgModule({
  declarations: [
    // Add your components here
  ],
  imports: [
    BrowserModule,
    JoinGameModule
  ],
  providers: [],
  bootstrap: []
})
export class AppModule { }
