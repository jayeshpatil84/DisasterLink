import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Login } from './components/login/login';
import { Register } from './components/register/register';
import { Dashboard } from './components/dashboard/dashboard';
import { VolunteerDashboard } from './components/volunteer-dashboard/volunteer-dashboard';
import { VictimMySos } from './components/victim-my-sos/victim-my-sos';
import { SosList } from './components/sos-list/sos-list';
import { SosForm } from './components/sos-form/sos-form';
import { NotFound } from './components/not-found/not-found';
import { authGuard } from './guards/auth-guard';
import { roleGuard } from './guards/role.guard';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Register },

  // Role-specific primary routes
  {
    path: 'officer/dashboard',
    component: Dashboard,
    canActivate: [roleGuard],
    data: { expectedRole: 'OFFICER' },
  },
  {
    path: 'volunteer/dashboard',
    component: VolunteerDashboard,
    canActivate: [roleGuard],
    data: { expectedRole: 'VOLUNTEER' },
  },
  {
    path: 'victim/my-sos',
    component: VictimMySos,
    canActivate: [roleGuard],
    data: { expectedRole: 'VICTIM' },
  },

  // Shared / generic routes
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'reports', component: SosList, canActivate: [authGuard] },
  { path: 'reports/new', component: SosForm, canActivate: [authGuard] },
  { path: '**', component: NotFound },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}