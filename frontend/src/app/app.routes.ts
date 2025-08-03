import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { UsersComponent } from './components/users/users.component';
import { ReportBuilderComponent } from './components/report-builder/report-builder.component';
import { ReportQueueComponent } from './components/report-queue/report-queue.component';
import { SimpleBusinessBuilderComponent } from './components/simple-business-builder/simple-business-builder.component';
import { DataIngestionComponent } from './components/data-ingestion/data-ingestion.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'users', component: UsersComponent },
  { path: 'report-builder', component: ReportBuilderComponent },
  { path: 'report-queue', component: ReportQueueComponent },
  { path: 'business-reports', component: SimpleBusinessBuilderComponent },
  { path: 'data-ingestion', component: DataIngestionComponent }
];
