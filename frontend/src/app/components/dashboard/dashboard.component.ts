import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions, ChartType } from 'chart.js';
import { ReportService } from '../../services/report.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    BaseChartDirective
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  dashboardStats: any = {};
  reports: any[] = [];
  
  public lineChartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Revenue',
        fill: true,
        tension: 0.5,
        borderColor: 'rgb(75, 192, 192)',
        backgroundColor: 'rgba(75, 192, 192, 0.2)'
      }
    ]
  };
  
  public lineChartOptions: ChartOptions<'line'> = {
    responsive: true
  };
  
  public lineChartLegend = true;

  displayedColumns: string[] = ['name', 'type', 'createdBy', 'createdAt', 'actions'];

  constructor(private reportService: ReportService) {}

  ngOnInit(): void {
    this.loadDashboardData();
    this.loadReports();
  }

  loadDashboardData(): void {
    this.reportService.getDashboardStats().subscribe(data => {
      this.dashboardStats = data;
      this.updateChartData(data.chartData);
    });
  }

  loadReports(): void {
    this.reportService.getAllReports().subscribe(data => {
      this.reports = data;
    });
  }

  updateChartData(chartData: any[]): void {
    this.lineChartData.labels = chartData.map(item => item.name);
    this.lineChartData.datasets[0].data = chartData.map(item => item.value);
  }

  createNewReport(): void {
    const newReport = {
      name: 'New Report ' + (this.reports.length + 1),
      description: 'Auto-generated report',
      type: 'Sales',
      createdBy: 'Admin'
    };
    
    this.reportService.createReport(newReport).subscribe(report => {
      this.reports.push(report);
      this.loadDashboardData();
    });
  }

  deleteReport(id: number): void {
    this.reportService.deleteReport(id).subscribe(() => {
      this.reports = this.reports.filter(report => report.id !== id);
      this.loadDashboardData();
    });
  }
}
