import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule
  ],
  template: `
    <mat-toolbar color="primary">
      <span>Reporting Application</span>
      <span class="spacer"></span>
      <button mat-button routerLink="/dashboard">
        <mat-icon>dashboard</mat-icon>
        Dashboard
      </button>
      <button mat-button routerLink="/report-builder">
        <mat-icon>build</mat-icon>
        Report Builder
      </button>
      <button mat-button routerLink="/business-reports">
        <mat-icon>business</mat-icon>
        Business Reports
      </button>
      <button mat-button routerLink="/report-queue">
        <mat-icon>queue</mat-icon>
        Queue
      </button>
      <button mat-button routerLink="/users">
        <mat-icon>people</mat-icon>
        Users
      </button>
    </mat-toolbar>
  `,
  styles: [`
    .spacer {
      flex: 1 1 auto;
    }
    button {
      margin: 0 8px;
    }
  `]
})
export class NavigationComponent {}
