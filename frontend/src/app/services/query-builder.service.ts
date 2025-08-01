import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { 
  QueryConfig, 
  TableMetadata, 
  ColumnMetadata, 
  JoinMetadata,
  ReportRequest,
  QueueStats
} from '../models/report-config.model';

@Injectable({
  providedIn: 'root'
})
export class QueryBuilderService {
  private apiUrl = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  getAvailableTables(): Observable<TableMetadata[]> {
    return this.http.get<TableMetadata[]>(`${this.apiUrl}/metadata/tables`);
  }

  getTableColumns(tableName: string): Observable<ColumnMetadata[]> {
    return this.http.get<ColumnMetadata[]>(`${this.apiUrl}/metadata/tables/${tableName}/columns`);
  }

  getFilterableColumns(tableName: string): Observable<ColumnMetadata[]> {
    return this.http.get<ColumnMetadata[]>(`${this.apiUrl}/metadata/tables/${tableName}/filters`);
  }

  getAvailableJoins(tableName: string): Observable<JoinMetadata[]> {
    return this.http.get<JoinMetadata[]>(`${this.apiUrl}/metadata/tables/${tableName}/joins`);
  }

  previewQuery(queryConfig: QueryConfig): Observable<{sql: string}> {
    return this.http.post<{sql: string}>(`${this.apiUrl}/preview`, queryConfig);
  }

  buildReport(userId: number, reportName: string, queryConfig: QueryConfig): Observable<ReportRequest> {
    return this.http.post<ReportRequest>(`${this.apiUrl}/build`, {
      userId,
      reportName,
      queryConfig
    });
  }

  getQueueStatus(): Observable<ReportRequest[]> {
    return this.http.get<ReportRequest[]>(`${this.apiUrl}/queue/status`);
  }

  getQueueStats(): Observable<QueueStats> {
    return this.http.get<QueueStats>(`${this.apiUrl}/queue/stats`);
  }

  getUserReports(userId: number): Observable<ReportRequest[]> {
    return this.http.get<ReportRequest[]>(`${this.apiUrl}/user/${userId}`);
  }

  getQueuePosition(reportId: number): Observable<{position: number}> {
    return this.http.get<{position: number}>(`${this.apiUrl}/request/${reportId}/position`);
  }

  initializeMetadata(): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/metadata/initialize`, {});
  }
}
