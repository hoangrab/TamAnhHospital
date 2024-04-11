import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { storageUtils } from '../utils/storage';

@Injectable()
export class AuthInterceptorInterceptor implements HttpInterceptor {

  constructor() {}
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    request = request.clone({
      setHeaders: {
        Authorization : `Bearer ${JSON.parse(storageUtils.get('jwt'))}`
      }
    })
    return next.handle(request);
  }
}
