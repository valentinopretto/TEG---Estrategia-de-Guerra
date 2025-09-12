import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class ObjectiveService {
  private apiUrl = 'http://localhost:8080/api/objectives';

  constructor(private http: HttpClient) {}

  getObjectiveById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  getAllObjectives(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  createObjective(objective: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, objective);
  }

  updateObjective(id: number, objective: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, objective);
  }

  deleteObjective(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
