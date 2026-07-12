import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Login } from './components/login/login';
import { Register } from './components/register/register';
import { Dashboard } from './components/dashboard/dashboard';
import { ReportList } from './components/report-list/report-list';
import { ReportForm } from './components/report-form/report-form';
import { NotFound } from './components/not-found/not-found';
import { authGuard } from './guards/auth-guard';

const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'reports', component: ReportList, canActivate: [authGuard] },
  { path: 'reports/new', component: ReportForm, canActivate: [authGuard] },
  { path: 'reports/edit/:id', component: ReportForm, canActivate: [authGuard] },
  { path: '**', component: NotFound },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
