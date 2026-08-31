import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { provideHttpClient, withFetch, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';

// Components
import { Navbar } from './components/navbar/navbar';
import { Login } from './components/login/login';
import { Register } from './components/register/register';
import { Dashboard } from './components/dashboard/dashboard';
import { VolunteerDashboard } from './components/volunteer-dashboard/volunteer-dashboard';
import { VictimMySos } from './components/victim-my-sos/victim-my-sos';
import { SosList } from './components/sos-list/sos-list';
import { SosForm } from './components/sos-form/sos-form';
import { NotFound } from './components/not-found/not-found';

// Legacy stubs (redirect to v2.0 routes — kept to avoid routing errors)
import { ReportForm } from './components/report-form/report-form';
import { ReportList } from './components/report-list/report-list';

import { AuthInterceptor } from './interceptors/auth-interceptor';

@NgModule({
  declarations: [
    App,
    Navbar,
    Login,
    Register,
    Dashboard,
    VolunteerDashboard,
    VictimMySos,
    SosList,
    SosForm,
    NotFound,
    ReportForm,  // legacy stub
    ReportList,  // legacy stub
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  providers: [
    provideHttpClient(withFetch(), withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ],
  bootstrap: [App]
})
export class AppModule {}
