import { Injectable, Injector } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from '../services/auth';

/**
 * Attaches the Bearer JWT token only to DisasterLink API requests.
 * AuthService is resolved lazily to avoid a circular DI:
 * AuthService → HttpClient → interceptor → AuthService.
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private injector: Injector) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const isApiRequest =
      req.url.startsWith(environment.apiUrl) || req.url.startsWith('/api');

    if (!isApiRequest) {
      return next.handle(req);
    }

    const token = this.injector.get(AuthService).getToken();
    if (token) {
      return next.handle(
        req.clone({
          setHeaders: { Authorization: `Bearer ${token}` },
        })
      );
    }

    return next.handle(req);
  }
}
