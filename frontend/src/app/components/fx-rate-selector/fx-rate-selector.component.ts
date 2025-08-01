import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { BusinessMetadataService } from '../../services/business-metadata.service';

@Component({
  selector: 'app-fx-rate-selector',
  standalone: true,
  imports: [CommonModule, FormsModule, MatSelectModule, MatFormFieldModule, MatChipsModule, MatCardModule],
  template: `
    <mat-card class="fx-rate-selector">
      <mat-card-header>
        <mat-card-title>FX Rate Selection</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <div class="selection-row">
          <mat-form-field>
            <mat-label>Currency</mat-label>
            <mat-select [(value)]="selectedCurrency" (selectionChange)="onSelectionChange()">
              <mat-option *ngFor="let currency of availableCurrencies" [value]="currency">
                {{currency}}
              </mat-option>
            </mat-select>
          </mat-form-field>
          
          <mat-form-field>
            <mat-label>Year</mat-label>
            <mat-select [(value)]="selectedYear" (selectionChange)="onSelectionChange()">
              <mat-option *ngFor="let year of availableYears" [value]="year">
                {{year}}
              </mat-option>
            </mat-select>
          </mat-form-field>
        </div>
        
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
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .fx-rate-selector {
      margin: 16px 0;
    }
    .selection-row {
      display: flex;
      gap: 16px;
      margin-bottom: 16px;
    }
    .selection-row mat-form-field {
      flex: 1;
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
  `]
})
export class FxRateSelectorComponent implements OnInit {
  @Output() selectionChange = new EventEmitter<any>();
  
  availableCurrencies: string[] = [];
  availableYears: number[] = [];
  selectedCurrency = '';
  selectedYear: number | null = null;
  selectedMonths: string[] = [];
  monthlyRates = ['m1_rate', 'm2_rate', 'm3_rate', 'm4_rate', 'm5_rate', 'm6_rate',
                  'm7_rate', 'm8_rate', 'm9_rate', 'm10_rate', 'm11_rate', 'm12_rate'];

  constructor(private businessMetadataService: BusinessMetadataService) {}

  ngOnInit() {
    this.loadCurrencies();
    this.loadYears();
  }

  loadCurrencies() {
    this.businessMetadataService.getAvailableCurrencies().subscribe(currencies => {
      this.availableCurrencies = currencies;
    });
  }

  loadYears() {
    this.businessMetadataService.getAvailableYears().subscribe(years => {
      this.availableYears = years;
    });
  }

  toggleMonth(month: string) {
    const index = this.selectedMonths.indexOf(month);
    if (index > -1) {
      this.selectedMonths.splice(index, 1);
    } else {
      this.selectedMonths.push(month);
    }
    this.onSelectionChange();
  }

  onSelectionChange() {
    this.selectionChange.emit({
      currency: this.selectedCurrency,
      year: this.selectedYear,
      monthlyRates: this.selectedMonths
    });
  }

  getMonthName(month: number): string {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
                   'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return months[month - 1];
  }
}
