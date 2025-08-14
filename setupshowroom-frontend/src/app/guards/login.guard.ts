import {Injectable} from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router} from '@angular/router';
import {jwtDecode} from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class LoginGuard implements CanActivate {
  constructor(private router: Router) {
  }

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const decodedToken: any = jwtDecode(token);
        const currentTime = Date.now() / 1000;
        const threshold = 30;
        if (decodedToken.exp && (decodedToken.exp - currentTime) > threshold) {
          this.router.navigate(['/feed']);
          return false;
        } else {
          localStorage.clear();
          this.router.navigate(['/login']);
        }
      } catch (error) {

      }
    }
    return true;
  }
}
