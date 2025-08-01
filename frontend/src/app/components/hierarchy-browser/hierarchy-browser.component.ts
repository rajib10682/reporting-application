import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTreeModule } from '@angular/material/tree';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { BusinessMetadataService } from '../../services/business-metadata.service';

interface HierarchyNode {
  id: string;
  name: string;
  parentId?: string;
  children?: HierarchyNode[];
}

@Component({
  selector: 'app-hierarchy-browser',
  standalone: true,
  imports: [
    CommonModule, 
    MatTreeModule, 
    MatIconModule, 
    MatButtonModule, 
    MatCheckboxModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    FormsModule
  ],
  template: `
    <mat-card class="hierarchy-browser">
      <mat-card-header>
        <mat-card-title>{{tableName | titlecase}} Hierarchy</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <mat-form-field>
          <mat-label>Period</mat-label>
          <mat-select [(value)]="periodId" (selectionChange)="loadHierarchy()">
            <mat-option *ngFor="let period of availablePeriods" [value]="period">
              {{period}}
            </mat-option>
          </mat-select>
        </mat-form-field>
        
        <mat-tree [dataSource]="dataSource" [treeControl]="treeControl" class="hierarchy-tree">
          <mat-tree-node *matTreeNodeDef="let node" matTreeNodePadding>
            <button mat-icon-button disabled></button>
            <mat-checkbox [checked]="isSelected(node)"
                         (change)="toggleSelection(node)">
              {{node.name}} ({{node.id}})
            </mat-checkbox>
          </mat-tree-node>
          
          <mat-tree-node *matTreeNodeDef="let node; when: hasChild" matTreeNodePadding>
            <button mat-icon-button matTreeNodeToggle>
              <mat-icon>{{treeControl.isExpanded(node) ? 'expand_less' : 'expand_more'}}</mat-icon>
            </button>
            <mat-checkbox [checked]="isSelected(node)"
                         (change)="toggleSelection(node)">
              {{node.name}} ({{node.id}})
            </mat-checkbox>
          </mat-tree-node>
        </mat-tree>
        
        <div class="selected-summary" *ngIf="selectedNodes.length > 0">
          <h4>Selected: {{selectedNodes.length}} items</h4>
          <div class="selected-items">
            <span *ngFor="let node of selectedNodes" class="selected-item">
              {{node.name}}
            </span>
          </div>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .hierarchy-browser {
      margin: 16px 0;
    }
    .hierarchy-tree {
      margin-top: 16px;
      max-height: 400px;
      overflow: auto;
    }
    .selected-summary {
      margin-top: 16px;
      padding: 16px;
      background-color: #f5f5f5;
      border-radius: 4px;
    }
    .selected-items {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-top: 8px;
    }
    .selected-item {
      background-color: #e3f2fd;
      padding: 4px 8px;
      border-radius: 16px;
      font-size: 12px;
    }
  `]
})
export class HierarchyBrowserComponent implements OnInit {
  @Input() tableName: string = '';
  @Input() periodId: string = '2024Q1';
  @Output() selectionChange = new EventEmitter<any[]>();
  
  treeControl = new NestedTreeControl<HierarchyNode>(node => node.children);
  dataSource = new MatTreeNestedDataSource<HierarchyNode>();
  selectedNodes: HierarchyNode[] = [];
  availablePeriods: string[] = ['2024Q1', '2024Q2', '2024Q3', '2024Q4', '2023Q1', '2023Q2', '2023Q3', '2023Q4'];

  constructor(private businessMetadataService: BusinessMetadataService) {}

  ngOnInit() {
    if (this.tableName) {
      this.loadHierarchy();
    }
  }

  loadHierarchy() {
    if (!this.tableName || !this.periodId) return;
    
    this.businessMetadataService.getHierarchy(this.tableName, this.periodId).subscribe({
      next: (data) => {
        const mockData = this.generateMockHierarchy();
        this.dataSource.data = mockData;
        this.treeControl.expandAll();
      },
      error: (error) => {
        console.error('Error loading hierarchy:', error);
        const mockData = this.generateMockHierarchy();
        this.dataSource.data = mockData;
        this.treeControl.expandAll();
      }
    });
  }

  private generateMockHierarchy(): HierarchyNode[] {
    const prefix = this.tableName === 'account_info' ? 'ACC' : 
                   this.tableName === 'segment_info' ? 'SEG' : 'GEO';
    
    return [
      {
        id: `${prefix}001`,
        name: `${prefix} Root Level`,
        children: [
          {
            id: `${prefix}001.1`,
            name: `${prefix} Child 1`,
            parentId: `${prefix}001`,
            children: [
              { id: `${prefix}001.1.1`, name: `${prefix} Grandchild 1.1`, parentId: `${prefix}001.1` },
              { id: `${prefix}001.1.2`, name: `${prefix} Grandchild 1.2`, parentId: `${prefix}001.1` }
            ]
          },
          {
            id: `${prefix}001.2`,
            name: `${prefix} Child 2`,
            parentId: `${prefix}001`,
            children: [
              { id: `${prefix}001.2.1`, name: `${prefix} Grandchild 2.1`, parentId: `${prefix}001.2` }
            ]
          }
        ]
      }
    ];
  }

  hasChild = (_: number, node: HierarchyNode) => !!node.children && node.children.length > 0;

  isSelected(node: HierarchyNode): boolean {
    return this.selectedNodes.some(selected => selected.id === node.id);
  }

  toggleSelection(node: HierarchyNode) {
    const index = this.selectedNodes.findIndex(selected => selected.id === node.id);
    if (index > -1) {
      this.selectedNodes.splice(index, 1);
    } else {
      this.selectedNodes.push(node);
    }
    this.selectionChange.emit(this.selectedNodes);
  }
}
