export interface QueryConfig {
  selectedTables: string[];
  selectedColumns: ColumnSelection[];
  filters: FilterCriteria[];
  sorting: SortCriteria[];
  limit?: number;
  exportFormat?: string;
}

export interface ColumnSelection {
  tableName: string;
  columnName: string;
  alias?: string;
  aggregation?: string; // SUM, COUNT, AVG, etc.
}

export interface FilterCriteria {
  tableName: string;
  columnName: string;
  operator: string; // =, !=, >, <, >=, <=, LIKE, IN, BETWEEN
  value: any;
  secondValue?: any; // for BETWEEN operator
  logicalOperator?: string; // AND, OR
}

export interface SortCriteria {
  tableName: string;
  columnName: string;
  direction: string; // ASC, DESC
}

export interface TableMetadata {
  id: number;
  tableName: string;
  displayName: string;
  description: string;
  isActive: boolean;
  columns?: ColumnMetadata[];
}

export interface ColumnMetadata {
  id: number;
  columnName: string;
  displayName: string;
  dataType: string;
  isFilterable: boolean;
  isSelectable: boolean;
  description: string;
}

export interface JoinMetadata {
  id: number;
  sourceTable: TableMetadata;
  targetTable: TableMetadata;
  sourceColumn: string;
  targetColumn: string;
  joinType: string;
  description: string;
}

export interface ReportRequest {
  id: number;
  userId: number;
  reportName: string;
  queryConfig: string;
  status: ReportStatus;
  priority: number;
  createdAt: string;
  startedAt?: string;
  completedAt?: string;
  errorMessage?: string;
  resultFilePath?: string;
}

export enum ReportStatus {
  PENDING = 'PENDING',
  QUEUED = 'QUEUED',
  EXECUTING = 'EXECUTING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

export interface ReportTemplate {
  id: number;
  userId: number;
  templateName: string;
  description: string;
  queryConfig: string;
  isPublic: boolean;
  createdAt: string;
}

export interface QueueStats {
  executing: number;
  queued: number;
}
