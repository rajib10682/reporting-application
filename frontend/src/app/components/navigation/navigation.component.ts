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
    <mat-toolbar class="professional-toolbar">
      <div class="toolbar-brand">
        <mat-icon class="brand-icon">analytics</mat-icon>
        <span class="brand-text">Reporting Application</span>
      </div>
      <span class="spacer"></span>
      <nav class="nav-buttons">
        <button mat-button routerLink="/dashboard" routerLinkActive="active-nav">
          <mat-icon>dashboard</mat-icon>
          <span>Dashboard</span>
        </button>
        <button mat-button routerLink="/report-builder" routerLinkActive="active-nav">
          <mat-icon>build</mat-icon>
          <span>Report Builder</span>
        </button>
        <button mat-button routerLink="/business-reports" routerLinkActive="active-nav">
          <mat-icon>business</mat-icon>
          <span>Business Reports</span>
        </button>
        <button mat-button routerLink="/report-queue" routerLinkActive="active-nav">
          <mat-icon>queue</mat-icon>
          <span>Queue</span>
        </button>
        <button mat-button routerLink="/data-ingestion" routerLinkActive="active-nav">
          <mat-icon>cloud_upload</mat-icon>
          <span>Data Ingestion</span>
        </button>
        <button mat-button routerLink="/adjustments" routerLinkActive="active-nav">
          <mat-icon>tune</mat-icon>
          <span>Adjustments</span>
        </button>
        <button mat-button routerLink="/users" routerLinkActive="active-nav">
          <mat-icon>people</mat-icon>
          <span>Users</span>
        </button>
      </nav>
    </mat-toolbar>
  `,
  styles: [`
    .professional-toolbar {
      background: var(--citi-blue) !important;
      color: white !important;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1) !important;
      height: 64px;
      padding: 0 24px;
    }
    
    .toolbar-brand {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    
    .brand-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
      color: white;
    }
    
    .brand-text {
      font-size: 1.4em;
      font-weight: 600;
      letter-spacing: 0.5px;
    }
    
    .spacer {
      flex: 1 1 auto;
    }
    
    .nav-buttons {
      display: flex;
      gap: 8px;
    }
    
    .nav-buttons button {
      color: rgba(255, 255, 255, 0.9) !important;
      border-radius: 8px !important;
      padding: 8px 16px !important;
      transition: all 0.3s ease !important;
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500 !important;
    }
    
    .nav-buttons button:hover {
      background: rgba(255, 255, 255, 0.1) !important;
      color: white !important;
      transform: translateY(-1px);
    }
    
    .nav-buttons button.active-nav {
      background: rgba(255, 255, 255, 0.15) !important;
      color: white !important;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .nav-buttons button mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }
    
    .nav-buttons button span {
      font-size: 14px;
    }
    
    @media (max-width: 768px) {
      .professional-toolbar {
        padding: 0 16px;
      }
      
      .nav-buttons button span {
        display: none;
      }
      
      .nav-buttons {
        gap: 4px;
      }
      
      .nav-buttons button {
        padding: 8px 12px !important;
      }
      
      .brand-text {
        font-size: 1.2em;
      }
    }
  `]
})
export class NavigationComponent {}
