import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatRadioModule } from '@angular/material/radio';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { AdjustmentService, AdjustmentSession, AdjustmentApprovalRequest } from '../../services/adjustment.service';
import { BusinessMetadataService } from '../../services/business-metadata.service';

@Component({
  selector: 'app-adjustment-management',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTabsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatRadioModule,
    MatInputModule,
    MatFormFieldModule,
    MatTableModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  template: `
    <div class="adjustment-container">
      <mat-card class="header-card">
        <mat-card-header>
          <mat-card-title>
            <mat-icon>tune</mat-icon>
            Adjustment Management
          </mat-card-title>
          <mat-card-subtitle>Upload and manage data adjustments with approval workflow</mat-card-subtitle>
        </mat-card-header>
      </mat-card>

      <mat-tab-group class="adjustment-tabs">
        <mat-tab label="Upload Adjustments" *ngIf="canSubmit">
          <div class="tab-content">
            <mat-card class="upload-card">
              <mat-card-header>
                <mat-card-title>Upload New Adjustment</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <div class="upload-form">
                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Select Scenario</mat-label>
                    <mat-select [(value)]="selectedScenario" required>
                      <mat-option *ngFor="let scenario of scenarios" [value]="scenario.scenario_id">
                        {{scenario.scenario_name}}
                      </mat-option>
                    </mat-select>
                  </mat-form-field>

                  <div class="upload-type-section">
                    <label class="section-label">Upload Type</label>
                    <mat-radio-group [(ngModel)]="uploadType" class="upload-type-group">
                      <mat-radio-button value="INCREMENTAL">Incremental (Add to existing data)</mat-radio-button>
                      <mat-radio-button value="REPLACE">Replace (Replace all scenario data)</mat-radio-button>
                    </mat-radio-group>
                  </div>

                  <div class="file-upload-section">
                    <label class="section-label">Adjustment File</label>
                    <div class="file-input-container">
                      <input type="file" #fileInput (change)="onFileSelected($event)" accept=".txt" class="file-input">
                      <button mat-stroked-button (click)="fileInput.click()" class="file-select-btn">
                        <mat-icon>attach_file</mat-icon>
                        Choose File
                      </button>
                      <span class="file-name" *ngIf="selectedFile">{{selectedFile.name}}</span>
                    </div>
                  </div>

                  <div class="template-section">
                    <button mat-outlined-button (click)="downloadTemplate()" class="template-btn">
                      <mat-icon>download</mat-icon>
                      Download Excel Template
                    </button>
                    <p class="template-note">Download the Excel template to see the required format for adjustment files.</p>
                  </div>

                  <div class="upload-actions">
                    <button mat-raised-button color="primary" 
                            (click)="uploadAdjustment()" 
                            [disabled]="!canUpload() || uploading"
                            class="upload-btn">
                      <mat-icon *ngIf="!uploading">cloud_upload</mat-icon>
                      <mat-spinner *ngIf="uploading" diameter="20"></mat-spinner>
                      {{uploading ? 'Uploading...' : 'Upload Adjustment'}}
                    </button>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <mat-tab label="Pending Approvals" *ngIf="canApprove">
          <div class="tab-content">
            <mat-card class="pending-card">
              <mat-card-header>
                <mat-card-title>Pending Adjustments</mat-card-title>
                <mat-card-subtitle>Review and approve/reject adjustment uploads</mat-card-subtitle>
              </mat-card-header>
              <mat-card-content>
                <div *ngIf="pendingAdjustments.length === 0" class="no-data">
                  <mat-icon>inbox</mat-icon>
                  <p>No pending adjustments</p>
                </div>
                <div *ngFor="let adjustment of pendingAdjustments" class="adjustment-item">
                  <div class="adjustment-header">
                    <h3>{{adjustment.filename}}</h3>
                    <mat-chip-set>
                      <mat-chip color="warn">{{adjustment.status}}</mat-chip>
                    </mat-chip-set>
                  </div>
                  <div class="adjustment-details">
                    <p><strong>Scenario:</strong> {{getScenarioName(adjustment.scenarioId)}}</p>
                    <p><strong>Upload Type:</strong> {{adjustment.uploadType}}</p>
                    <p><strong>Records:</strong> {{adjustment.totalRecords}} total, {{adjustment.successfulRecords}} successful</p>
                    <p><strong>Submitted:</strong> {{adjustment.createdAt | date:'medium'}}</p>
                    <p *ngIf="adjustment.errorMessage"><strong>Message:</strong> {{adjustment.errorMessage}}</p>
                  </div>
                  <div class="approval-actions">
                    <mat-form-field appearance="outline" class="comments-field">
                      <mat-label>Approval Comments</mat-label>
                      <textarea matInput [(ngModel)]="approvalComments[adjustment.sessionId]" rows="2"></textarea>
                    </mat-form-field>
                    <div class="action-buttons">
                      <button mat-raised-button color="primary" 
                              (click)="processApproval(adjustment.sessionId, 'APPROVE')"
                              [disabled]="processing">
                        <mat-icon>check</mat-icon>
                        Approve
                      </button>
                      <button mat-raised-button color="warn" 
                              (click)="processApproval(adjustment.sessionId, 'REJECT')"
                              [disabled]="processing">
                        <mat-icon>close</mat-icon>
                        Reject
                      </button>
                    </div>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <mat-tab label="Adjustment History">
          <div class="tab-content">
            <mat-card class="history-card">
              <mat-card-header>
                <mat-card-title>Adjustment History</mat-card-title>
                <mat-card-subtitle>View all adjustment uploads and their status</mat-card-subtitle>
              </mat-card-header>
              <mat-card-content>
                <div *ngIf="allAdjustments.length === 0" class="no-data">
                  <mat-icon>history</mat-icon>
                  <p>No adjustment history</p>
                </div>
                <div *ngFor="let adjustment of allAdjustments" class="adjustment-item">
                  <div class="adjustment-header">
                    <h3>{{adjustment.filename}}</h3>
                    <mat-chip-set>
                      <mat-chip [color]="getStatusColor(adjustment.status)">{{adjustment.status}}</mat-chip>
                    </mat-chip-set>
                  </div>
                  <div class="adjustment-details">
                    <p><strong>Scenario:</strong> {{getScenarioName(adjustment.scenarioId)}}</p>
                    <p><strong>Upload Type:</strong> {{adjustment.uploadType}}</p>
                    <p><strong>Records:</strong> {{adjustment.totalRecords}} total, {{adjustment.successfulRecords}} successful</p>
                    <p><strong>Submitted:</strong> {{adjustment.createdAt | date:'medium'}}</p>
                    <p *ngIf="adjustment.approvedAt"><strong>Approved:</strong> {{adjustment.approvedAt | date:'medium'}}</p>
                    <p *ngIf="adjustment.durationMs"><strong>Duration:</strong> {{adjustment.durationMs}}ms</p>
                    <p *ngIf="adjustment.approvalComments"><strong>Comments:</strong> {{adjustment.approvalComments}}</p>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: [`
    .adjustment-container {
      padding: 24px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .header-card {
      margin-bottom: 24px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
    }

    .header-card mat-card-title {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 1.5em;
    }

    .adjustment-tabs {
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .tab-content {
      padding: 24px;
    }

    .upload-card, .pending-card, .history-card {
      margin-bottom: 24px;
    }

    .upload-form {
      display: flex;
      flex-direction: column;
      gap: 24px;
    }

    .full-width {
      width: 100%;
    }

    .upload-type-section {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .section-label {
      font-weight: 500;
      color: #333;
      margin-bottom: 8px;
    }

    .upload-type-group {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .file-upload-section {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .file-input-container {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .file-input {
      display: none;
    }

    .file-select-btn {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .file-name {
      color: #666;
      font-style: italic;
    }

    .template-section {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .template-btn {
      align-self: flex-start;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .template-note {
      color: #666;
      font-size: 0.9em;
      margin: 0;
    }

    .upload-actions {
      display: flex;
      justify-content: flex-end;
    }

    .upload-btn {
      display: flex;
      align-items: center;
      gap: 8px;
      min-width: 160px;
    }

    .adjustment-item {
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 16px;
    }

    .adjustment-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }

    .adjustment-header h3 {
      margin: 0;
      color: #333;
    }

    .adjustment-details {
      margin-bottom: 16px;
    }

    .adjustment-details p {
      margin: 4px 0;
      color: #666;
    }

    .approval-actions {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .comments-field {
      width: 100%;
    }

    .action-buttons {
      display: flex;
      gap: 12px;
    }

    .no-data {
      text-align: center;
      padding: 48px;
      color: #666;
    }

    .no-data mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
      opacity: 0.5;
    }

    @media (max-width: 768px) {
      .adjustment-container {
        padding: 16px;
      }
      
      .upload-form {
        gap: 16px;
      }
      
      .file-input-container {
        flex-direction: column;
        align-items: flex-start;
      }
      
      .action-buttons {
        flex-direction: column;
      }
    }
  `]
})
export class AdjustmentManagementComponent implements OnInit {
  scenarios: any[] = [];
  selectedScenario: number | null = null;
  uploadType: string = 'INCREMENTAL';
  selectedFile: File | null = null;
  uploading: boolean = false;
  processing: boolean = false;
  
  pendingAdjustments: AdjustmentSession[] = [];
  allAdjustments: AdjustmentSession[] = [];
  approvalComments: { [key: number]: string } = {};
  
  currentUserId: number = 1;
  currentUserRole: string = 'admin';
  
  constructor(
    private adjustmentService: AdjustmentService,
    private businessMetadataService: BusinessMetadataService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.loadScenarios();
    this.loadAdjustments();
  }

  get canSubmit(): boolean {
    return this.currentUserRole === 'submitter' || this.currentUserRole === 'admin';
  }

  get canApprove(): boolean {
    return this.currentUserRole === 'approver' || this.currentUserRole === 'admin';
  }

  loadScenarios() {
    this.businessMetadataService.getAvailableScenarios().subscribe({
      next: (scenarios) => {
        this.scenarios = scenarios;
      },
      error: (error) => {
        this.snackBar.open('Failed to load scenarios', 'Close', { duration: 3000 });
      }
    });
  }

  loadAdjustments() {
    if (this.canApprove) {
      this.adjustmentService.getPendingAdjustments().subscribe({
        next: (adjustments) => {
          this.pendingAdjustments = adjustments;
        },
        error: (error) => {
          this.snackBar.open('Failed to load pending adjustments', 'Close', { duration: 3000 });
        }
      });
    }

    this.adjustmentService.getAllAdjustments().subscribe({
      next: (adjustments) => {
        this.allAdjustments = adjustments;
      },
      error: (error) => {
        this.snackBar.open('Failed to load adjustment history', 'Close', { duration: 3000 });
      }
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      if (file.name.endsWith('.txt')) {
        this.selectedFile = file;
      } else {
        this.snackBar.open('Please select a .txt file', 'Close', { duration: 3000 });
        event.target.value = '';
      }
    }
  }

  canUpload(): boolean {
    return this.selectedScenario !== null && this.selectedFile !== null && this.uploadType !== null;
  }

  uploadAdjustment() {
    if (!this.canUpload()) return;

    this.uploading = true;
    this.adjustmentService.uploadAdjustment(
      this.selectedFile!,
      this.selectedScenario!,
      this.uploadType,
      this.currentUserId
    ).subscribe({
      next: (status) => {
        this.uploading = false;
        this.snackBar.open('Adjustment uploaded successfully', 'Close', { duration: 3000 });
        this.resetForm();
        this.loadAdjustments();
      },
      error: (error) => {
        this.uploading = false;
        this.snackBar.open('Failed to upload adjustment: ' + error.error?.message || error.message, 'Close', { duration: 5000 });
      }
    });
  }

  processApproval(sessionId: number, action: string) {
    this.processing = true;
    const request: AdjustmentApprovalRequest = {
      sessionId: sessionId,
      action: action,
      approvedBy: this.currentUserId,
      comments: this.approvalComments[sessionId] || ''
    };

    this.adjustmentService.processApproval(request).subscribe({
      next: (status) => {
        this.processing = false;
        this.snackBar.open(`Adjustment ${action.toLowerCase()}d successfully`, 'Close', { duration: 3000 });
        this.loadAdjustments();
        delete this.approvalComments[sessionId];
      },
      error: (error) => {
        this.processing = false;
        this.snackBar.open('Failed to process approval: ' + error.error?.message || error.message, 'Close', { duration: 5000 });
      }
    });
  }

  downloadTemplate() {
    this.adjustmentService.downloadTemplate().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'adjustment_template.xlsx';
        link.click();
        window.URL.revokeObjectURL(url);
        this.snackBar.open('Template downloaded successfully', 'Close', { duration: 3000 });
      },
      error: (error) => {
        this.snackBar.open('Failed to download template', 'Close', { duration: 3000 });
      }
    });
  }

  getScenarioName(scenarioId: number): string {
    const scenario = this.scenarios.find(s => s.scenario_id === scenarioId);
    return scenario ? scenario.scenario_name : 'Unknown';
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'PENDING': return 'warn';
      case 'APPROVED': return 'primary';
      case 'REJECTED': return 'accent';
      default: return '';
    }
  }

  resetForm() {
    this.selectedScenario = null;
    this.uploadType = 'INCREMENTAL';
    this.selectedFile = null;
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }
}
