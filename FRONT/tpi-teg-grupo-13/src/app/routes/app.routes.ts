// src/app/routes/app.routes.ts
import { Routes } from '@angular/router';
import { LoginComponent } from '../features/auth/login/login.component';
import { RegisterComponent } from '../features/auth/register/register.component';
import { GameLobbyComponent } from '../features/lobby/components/game-lobby/game-lobby.component';
import { authGuard, guestGuard } from '../core/guards/auth.guard';
import { UserProfileComponent } from '../features/user/components/user-profile/user-profile.component';
import { GameMapComponent } from '../features/game/components/game-map/game-map.component';
import { ChangePasswordModalComponent } from '../features/user/components/change-password/change-password-modal.component';
import {JoinGameComponent} from '../features/join-game/components/join-game.component';
import {GameCreateComponent} from '../features/lobby/components/create-game-modal/create-game.component';
import {GameScreenComponent} from '../features/game/components/GameScreen/gameScreen.component';




export const routes: Routes = [

  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },

  {
    path: 'login',
    component: LoginComponent,
    //canActivate: [guestGuard]
  },
  {
    path: 'register',
    component: RegisterComponent,
    //canActivate: [guestGuard]
  },

  {
    path: 'lobby',
    component: GameLobbyComponent,
    //canActivate: [authGuard]
  },

  // ESTAS RUTAS TIENEN QUE IR ANTES DEL WILDCARD
  {
    path: 'join-game',
    //canActivate: [authGuard],
    component: JoinGameComponent
  },
  {
    path: 'create-game/:gameCode',
    component: GameCreateComponent,
    //canActivate: [guestGuard]
  },
  {
    path: 'create-game',
    component: GameCreateComponent,
  },
  {
    path: 'game/:gameCode',
    component: GameScreenComponent,
    canActivate: [authGuard]

  },

  {
    path: 'map',
    component: GameMapComponent,
  },

  {
    path: 'settings',
    component: UserProfileComponent,
    canActivate: [authGuard]
  },

  {
    path: 'change-password-modal',
    component: ChangePasswordModalComponent,
    canActivate: [authGuard]
  },

  // EL WILDCARD SIEMPRE VA AL FINAL
  {
    path: '**',
    redirectTo: '/login'
  }
];
