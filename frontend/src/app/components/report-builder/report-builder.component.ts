import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { QueryBuilderService } from '../../services/query-builder.service';
import { 
  QueryConfig, 
  TableMetadata, 
  ColumnMetadata, 
  ColumnSelection,
  FilterCriteria,
  SortCriteria
} from '../../models/report-config.model';

@Component({
  selector: 'app-report-builder',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatSelectModule,
    MatInputModule,
    MatFormFieldModule,
    MatChipsModule,
    MatIconModule,
    MatExpansionModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './report-builder.component.html',
  styleUrl: './report-builder.component.scss'
})
export class ReportBuilderComponent implements OnInit {
  availableTables: TableMetadata[] = [];
  selectedTables: string[] = [];
  availableColumns: ColumnMetadata[] = [];
  selectedColumns: ColumnSelection[] = [];
  filters: FilterCriteria[] = [];
  sorting: SortCriteria[] = [];
  
  reportName = '';
  queryPreview = '';
  isLoading = false;
  
  queryConfig: QueryConfig = {
    selectedTables: [],
    selectedColumns: [],
    filters: [],
    sorting: []
  };

  showBusinessComponents = false;

  filterOperators = [
    { value: '=', label: 'Equals' },
    { value: '!=', label: 'Not Equals' },
    { value: '>', label: 'Greater Than' },
    { value: '<', label: 'Less Than' },
    { value: '>=', label: 'Greater Than or Equal' },
    { value: '<=', label: 'Less Than or Equal' },
    { value: 'LIKE', label: 'Contains' },
    { value: 'IN', label: 'In List' },
    { value: 'BETWEEN', label: 'Between' }
  ];

  aggregationFunctions = [
    { value: '', label: 'None' },
    { value: 'COUNT', label: 'Count' },
    { value: 'SUM', label: 'Sum' },
    { value: 'AVG', label: 'Average' },
    { value: 'MIN', label: 'Minimum' },
    { value: 'MAX', label: 'Maximum' }
  ];

  constructor(
    private queryBuilderService: QueryBuilderService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadAvailableTables();
  }

  loadAvailableTables() {
    this.isLoading = true;
    this.queryBuilderService.getAvailableTables().subscribe({
      next: (tables) => {
        this.availableTables = tables;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading tables:', error);
        this.snackBar.open('Error loading tables', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onTableSelectionChange() {
    this.loadColumnsForSelectedTables();
    this.updateQueryConfig();
  }

  loadColumnsForSelectedTables() {
    this.availableColumns = [];
    
    this.selectedTables.forEach(tableName => {
      this.queryBuilderService.getTableColumns(tableName).subscribe({
        next: (columns) => {
          this.availableColumns.push(...columns);
        },
        error: (error) => {
          console.error(`Error loading columns for ${tableName}:`, error);
        }
      });
    });
  }

  addColumn() {
    this.selectedColumns.push({
      tableName: '',
      columnName: '',
      alias: '',
      aggregation: ''
    });
    this.updateQueryConfig();
  }

  removeColumn(index: number) {
    this.selectedColumns.splice(index, 1);
    this.updateQueryConfig();
  }

  addFilter() {
    this.filters.push({
      tableName: '',
      columnName: '',
      operator: '=',
      value: '',
      logicalOperator: 'AND'
    });
    this.updateQueryConfig();
  }

  removeFilter(index: number) {
    this.filters.splice(index, 1);
    this.updateQueryConfig();
  }

  addSort() {
    this.sorting.push({
      tableName: '',
      columnName: '',
      direction: 'ASC'
    });
    this.updateQueryConfig();
  }

  removeSort(index: number) {
    this.sorting.splice(index, 1);
    this.updateQueryConfig();
  }

  updateQueryConfig() {
    this.queryConfig = {
      selectedTables: this.selectedTables,
      selectedColumns: this.selectedColumns,
      filters: this.filters,
      sorting: this.sorting,
      limit: 1000,
      exportFormat: 'CSV'
    };
  }

  previewQuery() {
    if (this.selectedTables.length === 0) {
      this.snackBar.open('Please select at least one table', 'Close', { duration: 3000 });
      return;
    }

    this.isLoading = true;
    this.queryBuilderService.previewQuery(this.queryConfig).subscribe({
      next: (response) => {
        this.queryPreview = response.sql;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error previewing query:', error);
        this.snackBar.open('Error generating query preview', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  buildReport() {
    if (!this.reportName.trim()) {
      this.snackBar.open('Please enter a report name', 'Close', { duration: 3000 });
      return;
    }

    if (this.selectedTables.length === 0) {
      this.snackBar.open('Please select at least one table', 'Close', { duration: 3000 });
      return;
    }

    this.isLoading = true;
    const userId = 1; // TODO: Get from authentication service
    
    this.queryBuilderService.buildReport(userId, this.reportName, this.queryConfig).subscribe({
      next: (reportRequest) => {
        this.snackBar.open(`Report "${this.reportName}" submitted successfully!`, 'Close', { duration: 5000 });
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error building report:', error);
        this.snackBar.open('Error submitting report', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  getColumnsForTable(tableName: string): ColumnMetadata[] {
    return this.availableColumns.filter(col => 
      this.availableTables.find(table => table.tableName === tableName)?.columns?.includes(col)
    );
  }

  onTableSelectionChangeEnhanced() {
    this.onTableSelectionChange();
    this.checkForBusinessTables();
  }

  checkForBusinessTables() {
    const businessTables = ['fxrate_info', 'scenario_info', 'account_info', 'segment_info', 'geography_info', 'goc_info'];
    this.showBusinessComponents = this.selectedTables.some(table => businessTables.includes(table));
  }

  previewBusinessQuery() {
    this.snackBar.open('Business query preview coming soon!', 'Close', { duration: 3000 });
  }
}
