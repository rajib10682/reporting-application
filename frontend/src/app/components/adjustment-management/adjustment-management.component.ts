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
import { AdjustmentService, AdjustmentSession, AdjustmentApprovalRequest, UserInfo } from '../../services/adjustment.service';
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
  templateUrl: './adjustment-management.component.html',
  styleUrls: ['./adjustment-management.component.scss']
})
export class AdjustmentManagementComponent implements OnInit {
  scenarios: any[] = [];
  selectedScenario: number | null = null;
  uploadType: string = 'incremental';
  selectedFile: File | null = null;
  uploading: boolean = false;
  processing: boolean = false;
  
  pendingAdjustments: AdjustmentSession[] = [];
  allAdjustments: AdjustmentSession[] = [];
  approvalComments: { [key: number]: string } = {};
  
  users: UserInfo[] = [];
  submitters: UserInfo[] = [];
  approvers: UserInfo[] = [];
  selectedSubmitter: UserInfo | null = null;
  selectedApprover: UserInfo | null = null;
  
  constructor(
    private adjustmentService: AdjustmentService,
    private businessMetadataService: BusinessMetadataService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.loadScenarios();
    this.loadAdjustments();
    this.loadUsers();
  }

  loadUsers() {
    this.adjustmentService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.submitters = users.filter(u => u.role === 'submitter' || u.role === 'admin');
        this.approvers = users.filter(u => u.role === 'approver' || u.role === 'admin');
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.snackBar.open('Failed to load users', 'Close', { duration: 3000 });
      }
    });
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
    this.adjustmentService.getPendingAdjustments().subscribe({
      next: (adjustments) => {
        this.pendingAdjustments = adjustments;
      },
      error: (error) => {
        this.snackBar.open('Failed to load pending adjustments', 'Close', { duration: 3000 });
      }
    });

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
      if (file.name.endsWith('.xlsx') || file.name.endsWith('.txt')) {
        this.selectedFile = file;
      } else {
        this.snackBar.open('Please select a .xlsx or .txt file', 'Close', { duration: 3000 });
        event.target.value = '';
      }
    }
  }

  canUpload(): boolean {
    return this.selectedScenario !== null && this.selectedFile !== null && 
           this.uploadType !== null && this.selectedSubmitter !== null;
  }

  uploadAdjustment() {
    if (!this.canUpload()) return;

    this.uploading = true;
    this.adjustmentService.uploadAdjustment(
      this.selectedFile!,
      this.selectedScenario!,
      this.uploadType,
      this.selectedSubmitter!.userId
    ).subscribe({
      next: (status) => {
        this.uploading = false;
        this.snackBar.open('Adjustment uploaded successfully', 'Close', { duration: 3000 });
        this.resetForm();
        this.loadAdjustments();
      },
      error: (error) => {
        this.uploading = false;
        this.snackBar.open('Failed to upload adjustment: ' + (error.error?.message || error.message), 'Close', { duration: 5000 });
      }
    });
  }

  processApproval(sessionId: number, action: string) {
    if (!this.selectedApprover) {
      this.snackBar.open('Please select an approver', 'Close', { duration: 3000 });
      return;
    }
    
    this.processing = true;
    const request: AdjustmentApprovalRequest = {
      sessionId: sessionId,
      action: action,
      approvedBy: this.selectedApprover.userId,
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
        this.snackBar.open('Failed to process approval: ' + (error.error?.message || error.message), 'Close', { duration: 5000 });
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
    this.uploadType = 'incremental';
    this.selectedFile = null;
    this.selectedSubmitter = null;
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  get canSubmit(): boolean {
    return this.submitters.length > 0;
  }

  get canApprove(): boolean {
    return this.approvers.length > 0;
  }
}
