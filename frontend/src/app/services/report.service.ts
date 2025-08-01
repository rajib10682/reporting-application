import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAllReports(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/api/reports`);
  }

  getReportById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/api/reports/${id}`);
  }

  createReport(report: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/api/reports`, report);
  }

  deleteReport(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/api/reports/${id}`);
  }

  getDashboardStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/api/reports/dashboard-stats`);
  }

  getReportData(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/api/reports/${id}/data`);
  }
}
