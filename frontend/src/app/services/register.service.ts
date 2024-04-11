import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../environment/environment.dev';

@Injectable({
  providedIn: 'root'
})
export class RegisterService {
  apiUrl = environment.apiBaseUrl;

  constructor(private http : HttpClient) { }
}
