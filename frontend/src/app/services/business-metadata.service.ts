import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BusinessMetadataService {
  private apiUrl = `${environment.apiUrl}/api/business-metadata`;

  constructor(private http: HttpClient) {}

  getAvailableCurrencies(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/fxrates/currencies`);
  }

  getAvailableYears(): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/fxrates/years`);
  }

  getAvailableScenarios(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/scenarios`);
  }

  getAvailablePeriods(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/periods`);
  }

  getHierarchy(tableName: string, periodId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/hierarchies/${tableName}?periodId=${periodId}`);
  }

  buildBusinessQuery(config: any): Observable<{sql: string, queryType: string}> {
    return this.http.post<{sql: string, queryType: string}>(`${this.apiUrl}/build-business-query`, config);
  }

  validateCompositeJoin(sourceTable: string, targetTable: string, joinColumns: string[]): Observable<{valid: boolean, message: string}> {
    return this.http.post<{valid: boolean, message: string}>(`${this.apiUrl}/validate-composite-join`, {
      sourceTable,
      targetTable,
      joinColumns
    });
  }

  getTableRelationships(): Observable<any> {
    return this.http.get(`${this.apiUrl}/table-relationships`);
  }
}
