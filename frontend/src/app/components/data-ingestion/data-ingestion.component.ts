import { Component, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

interface IngestionStatus {
  status: string;
  totalRecords: number;
  successfulRecords: number;
  failedRecords: number;
  message: string;
}

@Component({
  selector: 'app-data-ingestion',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule,
    MatTableModule,
    MatChipsModule
  ],
  templateUrl: './data-ingestion.component.html',
  styleUrls: ['./data-ingestion.component.scss']
})
export class DataIngestionComponent {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  
  selectedFile: File | null = null;
  isUploading = false;
  uploadProgress = 0;
  ingestionHistory: IngestionStatus[] = [];
  
  displayedColumns: string[] = ['timestamp', 'filename', 'status', 'records', 'message'];

  constructor(
    private http: HttpClient,
    private snackBar: MatSnackBar
  ) {}

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.type === 'text/plain' || file.name.endsWith('.txt')) {
        this.selectedFile = file;
        this.showMessage(`Selected file: ${file.name}`, 'success');
      } else {
        this.showMessage('Please select a .txt file', 'error');
        event.target.value = '';
      }
    }
  }

  uploadFile(): void {
    if (!this.selectedFile) {
      this.showMessage('Please select a file first', 'error');
      return;
    }

    this.isUploading = true;
    this.uploadProgress = 0;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    const progressInterval = setInterval(() => {
      if (this.uploadProgress < 90) {
        this.uploadProgress += 10;
      }
    }, 200);

    this.http.post<IngestionStatus>(`${environment.apiUrl}/api/data/feed/upload`, formData)
      .subscribe({
        next: (response) => {
          clearInterval(progressInterval);
          this.uploadProgress = 100;
          
          const historyEntry = {
            ...response,
            timestamp: new Date(),
            filename: this.selectedFile?.name || 'Unknown'
          };
          this.ingestionHistory.unshift(historyEntry);
          
          this.isUploading = false;
          this.selectedFile = null;
          
          if (response.status === 'COMPLETED') {
            this.showMessage(`Successfully processed ${response.successfulRecords} records`, 'success');
          } else if (response.status === 'COMPLETED_WITH_ERRORS') {
            this.showMessage(`Processed with ${response.failedRecords} errors. Check details below.`, 'warning');
          } else {
            this.showMessage(`Upload failed: ${response.message}`, 'error');
          }
          
          this.clearFileInput();
        },
        error: (error) => {
          clearInterval(progressInterval);
          this.isUploading = false;
          this.uploadProgress = 0;
          this.showMessage(`Upload failed: ${error.error?.message || error.message}`, 'error');
          
          this.clearFileInput();
        }
      });
  }

  private showMessage(message: string, type: 'success' | 'error' | 'warning'): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      panelClass: [`snackbar-${type}`]
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'primary';
      case 'COMPLETED_WITH_ERRORS': return 'warn';
      case 'FAILED': return 'warn';
      default: return 'accent';
    }
  }

  formatTimestamp(timestamp: Date): string {
    return new Date(timestamp).toLocaleString();
  }

  triggerFileInput(): void {
    this.fileInput.nativeElement.click();
  }

  clearFileInput(): void {
    this.selectedFile = null;
    this.fileInput.nativeElement.value = '';
  }
}
