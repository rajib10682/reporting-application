import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';

import { QueryBuilderService } from '../../services/query-builder.service';
import { ReportRequest, ReportStatus, QueueStats } from '../../models/report-config.model';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-report-queue',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressBarModule,
    MatSnackBarModule
  ],
  templateUrl: './report-queue.component.html',
  styleUrl: './report-queue.component.scss'
})
export class ReportQueueComponent implements OnInit, OnDestroy {
  queueStatus: ReportRequest[] = [];
  queueStats: QueueStats = { executing: 0, queued: 0 };
  userReports: ReportRequest[] = [];
  displayedColumns: string[] = ['id', 'reportName', 'status', 'priority', 'createdAt', 'actions'];
  
  private refreshSubscription?: Subscription;
  
  constructor(
    private queryBuilderService: QueryBuilderService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadQueueData();
    this.startAutoRefresh();
  }

  ngOnDestroy() {
    if (this.refreshSubscription) {
      this.refreshSubscription.unsubscribe();
    }
  }

  loadQueueData() {
    this.queryBuilderService.getQueueStatus().subscribe({
      next: (status) => {
        this.queueStatus = status;
      },
      error: (error) => {
        console.error('Error loading queue status:', error);
        this.snackBar.open('Error loading queue status', 'Close', { duration: 3000 });
      }
    });

    this.queryBuilderService.getQueueStats().subscribe({
      next: (stats) => {
        this.queueStats = stats;
      },
      error: (error) => {
        console.error('Error loading queue stats:', error);
      }
    });

    const userId = 1; // TODO: Get from authentication service
    this.queryBuilderService.getUserReports(userId).subscribe({
      next: (reports) => {
        this.userReports = reports;
      },
      error: (error) => {
        console.error('Error loading user reports:', error);
      }
    });
  }

  startAutoRefresh() {
    this.refreshSubscription = interval(5000).subscribe(() => {
      this.loadQueueData();
    });
  }

  getStatusColor(status: ReportStatus): string {
    switch (status) {
      case ReportStatus.PENDING:
        return 'accent';
      case ReportStatus.QUEUED:
        return 'primary';
      case ReportStatus.EXECUTING:
        return 'warn';
      case ReportStatus.COMPLETED:
        return 'primary';
      case ReportStatus.FAILED:
        return 'warn';
      default:
        return 'primary';
    }
  }

  getStatusIcon(status: ReportStatus): string {
    switch (status) {
      case ReportStatus.PENDING:
        return 'schedule';
      case ReportStatus.QUEUED:
        return 'queue';
      case ReportStatus.EXECUTING:
        return 'play_circle';
      case ReportStatus.COMPLETED:
        return 'check_circle';
      case ReportStatus.FAILED:
        return 'error';
      default:
        return 'help';
    }
  }

  downloadReport(report: ReportRequest) {
    if (report.resultFilePath) {
      this.snackBar.open('Download functionality coming soon', 'Close', { duration: 3000 });
    }
  }

  getQueuePosition(reportId: number) {
    this.queryBuilderService.getQueuePosition(reportId).subscribe({
      next: (response) => {
        const position = response.position;
        if (position > 0) {
          this.snackBar.open(`Report is at position ${position} in queue`, 'Close', { duration: 3000 });
        } else {
          this.snackBar.open('Report is not in queue', 'Close', { duration: 3000 });
        }
      },
      error: (error) => {
        console.error('Error getting queue position:', error);
      }
    });
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }
}
