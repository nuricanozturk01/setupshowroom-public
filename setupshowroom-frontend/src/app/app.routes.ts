import {RouterModule, Routes} from '@angular/router';
import {NgModule} from '@angular/core';
import {LoginComponent} from './components/auth/login/login.component';
import {RegisterComponent} from './components/auth/register/register.component';
import {FeedComponent} from './components/feed/feed.component';
import {LayoutComponent} from './components/layout/layout.component';
import {ProfileComponent} from './components/profile/profile.component';
import {NotificationsComponent} from './components/notifications/notifications.component';
import {ShareSetupComponent} from './components/setup/share-setup/share-setup.component';
import {AuthGuard} from './guards/auth.guard';
import {MySetupComponent} from './components/setup/my-setup/my-setup.component';
import {ExploreComponent} from './components/explore/explore.component';
import {FavoriteComponent} from './components/favorite/favorite.component';
import {SetupUpdateComponent} from './components/setup/setup-update/setup-update.component';
import {SetupInfoComponent} from './components/setup/setup-info/setup-info.component';
import {PublicComponent} from './components/profile/public/public.component';
import {LoginGuard} from './guards/login.guard';

export const routes: Routes = [
  {path: '', redirectTo: '/login', pathMatch: 'full'},
  { path: 'login', component: LoginComponent, canActivate: [LoginGuard] },
  {path: 'register', component: RegisterComponent, canActivate: [LoginGuard]},
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      {path: 'explore', component: ExploreComponent},
      {path: 'feed', component: FeedComponent},
      {path: 'favorites', component: FavoriteComponent},
      {path: 'my-setups', component: MySetupComponent},
      {path: 'notifications', component: NotificationsComponent},
      {path: 'create-post', component: ShareSetupComponent},
      {path: 'profile', component: ProfileComponent},
      {path: 'profile/:username', component: PublicComponent},
      {path: 'setup/:id', component: SetupUpdateComponent},
      {path: 'setups/:id', component: SetupInfoComponent}
    ]
  },
  {path: '**', redirectTo: '/login', pathMatch: 'full'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: false})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
