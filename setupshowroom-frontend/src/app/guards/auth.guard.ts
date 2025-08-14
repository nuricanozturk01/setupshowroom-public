import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../components/auth/service/auth.service';
import { Observable, of } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AuthGuard implements CanActivate {
    constructor(
        private authService: AuthService,
        private router: Router
    ) {}

    canActivate(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot
    ): Observable<boolean> | Promise<boolean> | boolean {
        const token = this.authService.accessToken;

        if (!token) {
            this.redirectToLogin(state.url);
            return false;
        }

        return true;
    }

    private redirectToLogin(returnUrl: string): void {
        this.router.navigate(['/login'], {
            queryParams: { returnUrl }
        });
    }
}
