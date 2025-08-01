import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { BusinessMetadataService } from '../../services/business-metadata.service';
import { FxRateSelectorComponent } from '../fx-rate-selector/fx-rate-selector.component';
import { HierarchyBrowserComponent } from '../hierarchy-browser/hierarchy-browser.component';
import { BusinessQueryConfig } from '../../models/report-config.model';

@Component({
  selector: 'app-business-report-builder',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatSelectModule,
    MatInputModule,
    MatFormFieldModule,
    MatTabsModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    FxRateSelectorComponent,
    HierarchyBrowserComponent
  ],
  template: `
    <mat-card class="business-report-builder">
      <mat-card-header>
        <mat-card-title>Business Report Builder</mat-card-title>
        <mat-card-subtitle>Create reports using business metadata tables</mat-card-subtitle>
      </mat-card-header>
      <mat-card-content>
        <mat-tab-group>
          <mat-tab label="FX Rates">
            <div class="tab-content">
              <app-fx-rate-selector (selectionChange)="onFxRateSelectionChange($event)"></app-fx-rate-selector>
              <div class="query-actions">
                <button mat-raised-button color="primary" (click)="buildFxRateQuery()" [disabled]="isLoading">
                  Build FX Rate Query
                </button>
              </div>
            </div>
          </mat-tab>
          
          <mat-tab label="Hierarchies">
            <div class="tab-content">
              <mat-form-field>
                <mat-label>Select Hierarchy Type</mat-label>
                <mat-select [(value)]="selectedHierarchyType">
                  <mat-option value="account_info">Account Hierarchy</mat-option>
                  <mat-option value="segment_info">Segment Hierarchy</mat-option>
                  <mat-option value="geography_info">Geography Hierarchy</mat-option>
                </mat-select>
              </mat-form-field>
              
              <app-hierarchy-browser 
                *ngIf="selectedHierarchyType"
                [tableName]="selectedHierarchyType"
                (selectionChange)="onHierarchySelectionChange($event)">
              </app-hierarchy-browser>
              
              <div class="query-actions">
                <button mat-raised-button color="primary" (click)="buildHierarchyQuery()" [disabled]="isLoading">
                  Build Hierarchy Query
                </button>
              </div>
            </div>
          </mat-tab>
          
          <mat-tab label="GOC Analysis">
            <div class="tab-content">
              <p>GOC Analysis combines segment and geography data for comprehensive reporting.</p>
              <div class="query-actions">
                <button mat-raised-button color="primary" (click)="buildGocAnalysisQuery()" [disabled]="isLoading">
                  Build GOC Analysis Query
                </button>
              </div>
            </div>
          </mat-tab>
        </mat-tab-group>
        
        <div class="query-preview" *ngIf="queryPreview">
          <h3>Generated SQL Query:</h3>
          <pre>{{queryPreview}}</pre>
        </div>
        
        <div class="report-actions" *ngIf="queryPreview">
          <mat-form-field>
            <mat-label>Report Name</mat-label>
            <input matInput [(ngModel)]="reportName" placeholder="Enter report name">
          </mat-form-field>
          <button mat-raised-button color="accent" (click)="executeReport()" [disabled]="isLoading || !reportName">
            Execute Report
          </button>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .business-report-builder {
      margin: 16px;
      max-width: 1200px;
    }
    .tab-content {
      padding: 16px 0;
    }
    .query-actions {
      margin: 16px 0;
    }
    .query-preview {
      margin-top: 20px;
      padding: 16px;
      background-color: #f5f5f5;
      border-radius: 4px;
    }
    .query-preview pre {
      white-space: pre-wrap;
      font-family: 'Courier New', monospace;
    }
    .report-actions {
      display: flex;
      gap: 16px;
      align-items: center;
      margin-top: 16px;
    }
    .report-actions mat-form-field {
      flex: 1;
    }
  `]
})
export class BusinessReportBuilderComponent implements OnInit {
  selectedHierarchyType = '';
  fxRateConfig: any = {};
  hierarchySelection: any[] = [];
  queryPreview = '';
  reportName = '';
  isLoading = false;

  constructor(
    private businessMetadataService: BusinessMetadataService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {}

  onFxRateSelectionChange(config: any) {
    this.fxRateConfig = config;
  }

  onHierarchySelectionChange(selectedNodes: any[]) {
    this.hierarchySelection = selectedNodes;
  }

  buildFxRateQuery() {
    if (!this.fxRateConfig['currency'] || !this.fxRateConfig['year']) {
      this.snackBar.open('Please select currency and year', 'Close', { duration: 3000 });
      return;
    }

    this.isLoading = true;
    const config: BusinessQueryConfig = {
      queryType: 'fxrate',
      filters: {
        currency: this.fxRateConfig['currency'],
        year: this.fxRateConfig['year']
      },
      selectedColumns: ['fx_id', 'fx_name', 'currency', 'year', ...this.fxRateConfig['monthlyRates'] || []]
    };

    this.businessMetadataService.buildBusinessQuery(config).subscribe({
      next: (response: any) => {
        this.queryPreview = response.sql;
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error building FX rate query:', error);
        this.snackBar.open('Error building FX rate query', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  buildHierarchyQuery() {
    if (!this.selectedHierarchyType) {
      this.snackBar.open('Please select a hierarchy type', 'Close', { duration: 3000 });
      return;
    }

    this.isLoading = true;
    const config: BusinessQueryConfig = {
      queryType: 'hierarchical',
      tableName: this.selectedHierarchyType,
      includeHierarchy: true,
      filters: {
        period_id: '2024Q1'
      }
    };

    this.businessMetadataService.buildBusinessQuery(config).subscribe({
      next: (response: any) => {
        this.queryPreview = response.sql;
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error building hierarchy query:', error);
        this.snackBar.open('Error building hierarchy query', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  buildGocAnalysisQuery() {
    this.isLoading = true;
    const config: BusinessQueryConfig = {
      queryType: 'goc_analysis',
      filters: {
        period_id: '2024Q1'
      }
    };

    this.businessMetadataService.buildBusinessQuery(config).subscribe({
      next: (response: any) => {
        this.queryPreview = response.sql;
        this.isLoading = false;
      },
      error: (error: any) => {
        console.error('Error building GOC analysis query:', error);
        this.snackBar.open('Error building GOC analysis query', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  executeReport() {
    if (!this.reportName.trim()) {
      this.snackBar.open('Please enter a report name', 'Close', { duration: 3000 });
      return;
    }

    this.snackBar.open(`Report "${this.reportName}" submitted for execution!`, 'Close', { duration: 5000 });
  }
}
