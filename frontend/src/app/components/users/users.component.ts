import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule
  ],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss'
})
export class UsersComponent implements OnInit {
  users: any[] = [];
  displayedColumns: string[] = ['username', 'email', 'firstName', 'lastName', 'role', 'actions'];
  
  newUser = {
    username: '',
    email: '',
    firstName: '',
    lastName: '',
    role: 'User'
  };

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe(data => {
      this.users = data;
    });
  }

  createUser(): void {
    if (this.newUser.username && this.newUser.email) {
      this.userService.createUser(this.newUser).subscribe(user => {
        this.users.push(user);
        this.resetForm();
      });
    }
  }

  deleteUser(id: number): void {
    this.userService.deleteUser(id).subscribe(() => {
      this.users = this.users.filter(user => user.id !== id);
    });
  }

  resetForm(): void {
    this.newUser = {
      username: '',
      email: '',
      firstName: '',
      lastName: '',
      role: 'User'
    };
  }
}
