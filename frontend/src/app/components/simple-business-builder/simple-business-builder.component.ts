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
import { MatChipsModule } from '@angular/material/chips';
import { MatCheckboxModule } from '@angular/material/checkbox';

import { BusinessMetadataService } from '../../services/business-metadata.service';
import { BusinessQueryConfig } from '../../models/report-config.model';

@Component({
  selector: 'app-simple-business-builder',
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
    MatChipsModule,
    MatCheckboxModule
  ],
  template: `
    <mat-card class="simple-business-builder">
      <mat-card-header>
        <mat-card-title>Business Report Builder</mat-card-title>
        <mat-card-subtitle>Create reports using business metadata tables</mat-card-subtitle>
      </mat-card-header>
      <mat-card-content>
        <mat-tab-group>
          <mat-tab label="FX Rates">
            <div class="tab-content">
              <div class="fx-rate-section">
                <mat-form-field>
                  <mat-label>Currency</mat-label>
                  <mat-select [(value)]="selectedCurrency">
                    <mat-option *ngFor="let currency of availableCurrencies" [value]="currency">
                      {{currency}}
                    </mat-option>
                  </mat-select>
                </mat-form-field>
                
                <mat-form-field>
                  <mat-label>Year</mat-label>
                  <mat-select [(value)]="selectedYear">
                    <mat-option *ngFor="let year of availableYears" [value]="year">
                      {{year}}
                    </mat-option>
                  </mat-select>
                </mat-form-field>
                
                <div class="monthly-rates" *ngIf="selectedCurrency && selectedYear">
                  <h4>Select Monthly Rates:</h4>
                  <mat-chip-set>
                    <mat-chip *ngFor="let month of monthlyRates; let i = index" 
                              [class.selected]="selectedMonths.includes(month)"
                              (click)="toggleMonth(month)">
                      {{getMonthName(i + 1)}}
                    </mat-chip>
                  </mat-chip-set>
                </div>
              </div>
              
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
              
              <mat-form-field>
                <mat-label>Period</mat-label>
                <mat-select [(value)]="selectedPeriod">
                  <mat-option *ngFor="let period of availablePeriods" [value]="period">
                    {{period}}
                  </mat-option>
                </mat-select>
              </mat-form-field>
              
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
              <mat-form-field>
                <mat-label>Period</mat-label>
                <mat-select [(value)]="selectedPeriod">
                  <mat-option *ngFor="let period of availablePeriods" [value]="period">
                    {{period}}
                  </mat-option>
                </mat-select>
              </mat-form-field>
              
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
    .simple-business-builder {
      margin: 16px;
      max-width: 1200px;
    }
    .tab-content {
      padding: 16px 0;
    }
    .fx-rate-section {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    .fx-rate-section mat-form-field {
      max-width: 300px;
    }
    .monthly-rates h4 {
      margin: 16px 0 8px 0;
    }
    mat-chip {
      margin: 4px;
      cursor: pointer;
    }
    
    mat-chip.selected {
      background-color: #3f51b5;
      color: white;
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
export class SimpleBusinessBuilderComponent implements OnInit {
  selectedHierarchyType = '';
  selectedCurrency = '';
  selectedYear: number | null = null;
  selectedPeriod = '2024Q1';
  selectedMonths: string[] = [];
  queryPreview = '';
  reportName = '';
  isLoading = false;

  availableCurrencies = ['USD', 'EUR', 'GBP', 'JPY', 'CAD', 'AUD', 'CHF', 'CNY'];
  availableYears = [2020, 2021, 2022, 2023, 2024, 2025];
  availablePeriods = ['2024Q1', '2024Q2', '2024Q3', '2024Q4', '2023Q1', '2023Q2', '2023Q3', '2023Q4'];
  monthlyRates = ['m1_rate', 'm2_rate', 'm3_rate', 'm4_rate', 'm5_rate', 'm6_rate',
                  'm7_rate', 'm8_rate', 'm9_rate', 'm10_rate', 'm11_rate', 'm12_rate'];

  constructor(
    private businessMetadataService: BusinessMetadataService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {}

  toggleMonth(month: string) {
    const index = this.selectedMonths.indexOf(month);
    if (index > -1) {
      this.selectedMonths.splice(index, 1);
    } else {
      this.selectedMonths.push(month);
    }
  }

  getMonthName(month: number): string {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                   'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return months[month - 1];
  }

  buildFxRateQuery() {
    if (!this.selectedCurrency || !this.selectedYear) {
      this.snackBar.open('Please select currency and year', 'Close', { duration: 3000 });
      return;
    }

    this.isLoading = true;
    const config: BusinessQueryConfig = {
      queryType: 'fxrate',
      filters: {
        currency: this.selectedCurrency,
        year: this.selectedYear
      },
      selectedColumns: ['fx_id', 'fx_name', 'currency', 'year', ...this.selectedMonths]
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
        period_id: this.selectedPeriod
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
        period_id: this.selectedPeriod
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
