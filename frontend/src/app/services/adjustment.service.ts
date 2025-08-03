import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface UserInfo {
  userId: number;
  name: string;
  role: string;
}

export interface AdjustmentSession {
  sessionId: number;
  filename: string;
  scenarioId: number;
  uploadType: string;
  submittedBy: number;
  approvedBy?: number;
  startTime: string;
  endTime?: string;
  durationMs?: number;
  totalRecords?: number;
  successfulRecords?: number;
  failedRecords?: number;
  status: string;
  errorMessage?: string;
  approvalComments?: string;
  createdAt: string;
  approvedAt?: string;
}

export interface AdjustmentApprovalRequest {
  sessionId: number;
  action: string;
  approvedBy: number;
  comments?: string;
}

export interface IngestionStatus {
  sessionId: number;
  filename: string;
  status: string;
  message: string;
  totalRecords?: number;
  successfulRecords?: number;
  failedRecords?: number;
  startTime?: string;
  endTime?: string;
  durationMs?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AdjustmentService {
  private apiUrl = `${environment.apiUrl}/api/adjustments`;

  constructor(private http: HttpClient) {}

  uploadAdjustment(file: File, scenarioId: number, uploadType: string, submittedBy: number): Observable<IngestionStatus> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('scenarioId', scenarioId.toString());
    formData.append('uploadType', uploadType);
    formData.append('submittedBy', submittedBy.toString());

    return this.http.post<IngestionStatus>(`${this.apiUrl}/upload`, formData);
  }

  processApproval(request: AdjustmentApprovalRequest): Observable<IngestionStatus> {
    return this.http.post<IngestionStatus>(`${this.apiUrl}/approve`, request);
  }

  getPendingAdjustments(): Observable<AdjustmentSession[]> {
    return this.http.get<AdjustmentSession[]>(`${this.apiUrl}/pending`);
  }

  getUserAdjustments(userId: number): Observable<AdjustmentSession[]> {
    return this.http.get<AdjustmentSession[]>(`${this.apiUrl}/user/${userId}`);
  }

  getAllAdjustments(): Observable<AdjustmentSession[]> {
    return this.http.get<AdjustmentSession[]>(`${this.apiUrl}/all`);
  }

  downloadTemplate(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/template/download`, { responseType: 'blob' });
  }
  
  getAllUsers(): Observable<UserInfo[]> {
    return this.http.get<UserInfo[]>(`${this.apiUrl}/users`);
  }
  
  getSubmitters(): Observable<UserInfo[]> {
    return this.http.get<UserInfo[]>(`${this.apiUrl}/users/submitters`);
  }
  
  getApprovers(): Observable<UserInfo[]> {
    return this.http.get<UserInfo[]>(`${this.apiUrl}/users/approvers`);
  }
}
