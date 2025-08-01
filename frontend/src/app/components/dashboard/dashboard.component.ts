import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';

import { ReportService } from '../../services/report.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, MatCardModule, MatButtonModule, MatIconModule, MatTableModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  totalReports = 42;
  activeUsers = 8;
  queuedReports = 3;
  recentReports: any[] = [];

  displayedColumns: string[] = ['name', 'status', 'createdAt', 'actions'];

  constructor(private reportService: ReportService) {}

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    this.reportService.getDashboardStats().subscribe({
      next: (stats: any) => {
        this.totalReports = stats.totalReports || this.totalReports;
        this.activeUsers = stats.activeUsers || this.activeUsers;
        this.queuedReports = stats.queuedReports || this.queuedReports;
      },
      error: (error: any) => {
        console.error('Error loading dashboard stats:', error);
      }
    });

    this.reportService.getRecentReports().subscribe({
      next: (reports: any[]) => {
        this.recentReports = reports;
      },
      error: (error: any) => {
        console.error('Error loading recent reports:', error);
      }
    });
  }
}
