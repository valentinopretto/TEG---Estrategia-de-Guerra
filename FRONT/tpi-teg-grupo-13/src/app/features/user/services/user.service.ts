import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {environment} from '../../../../environments/environment';

export interface UserProfileDto {
  id: string;
  username: string;
  email: string;
  avatarUrl?: string;
  gamesPlayed: number;
  gamesWon: number;
  createdAt: Date;
  lastLogin?: Date;
  isActive: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly API_URL = `${environment.apiUrl}/user`;

  constructor(private http: HttpClient) {}

  // actualizar perfil
  updateUser(id: number, userData: Partial<UserProfileDto>): Observable<void> {
    return this.http.put<void>(`${this.API_URL}/${id}`, userData);
  }

  //obtener perfil
    getUserById(id: number): Observable<UserProfileDto> {
    return this.http.get<UserProfileDto>(`${this.API_URL}/${id}`);
  }

  changePassword(userId: number, passwordData: ChangePasswordDto): Observable<any> {
    return this.http.put(`${this.API_URL}/${userId}/change-password`, passwordData);
  }
}
export interface ChangePasswordDto {
  currentPassword: string;
  newPassword: string;
}

