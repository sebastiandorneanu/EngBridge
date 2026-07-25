import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AdminService, User } from '../services/admin.service';
import { AuthService } from '../services/auth.service';
import { UserFormComponent } from '../user-form/user-form.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, UserFormComponent],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {

  users: User[] = [];
  currentUser: any = null;
  errorMessage = '';
  successMessage = '';
  loading = false;
  showDeleteConfirm = false;
  userToDelete: User | null = null;

  showUserForm = false;
  selectedUser: User | null = null;
  activeTab: 'users' | 'courses' = 'users';

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone
  ) { }

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    console.log('Dashboard loaded, fetching users...');
    this.loadUsers();
  }

  loadUsers() {
    this.errorMessage = '';
    this.loading = true;
    console.log('Starting to load users...');

    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.ngZone.run(() => {
          console.log('Users loaded:', data);
          console.log('Array length:', data.length);
          console.log('Type of data:', typeof data);
          console.log('Is array?', Array.isArray(data));

          this.users = data.filter(user => String(user.uid) !== String(this.currentUser?.uid));
          this.loading = false;

          console.log('this.users after assignment:', this.users);
          console.log('this.users.length:', this.users.length);

          this.cdr.detectChanges();
        });
      },
      error: (err) => {
        this.ngZone.run(() => {
          console.error('Error loading users:', err);
          this.errorMessage = 'Failed to load users: ' + (err.message || 'Unknown error');
          this.loading = false;
          this.cdr.detectChanges();
        });
      }
    });
  }

  getAdminCount(): number {
    return this.users.filter(u => u.role === 'ADMIN').length;
  }

  getStudentCount(): number {
    return this.users.filter(u => u.role === 'STUDENT').length;
  }

  getRoleClass(role: string): string {
    return role.toLowerCase();
  }

  openCreateUser() {
  this.selectedUser = null;  // null = CREATE mode
  this.showUserForm = true;
  this.errorMessage = '';
}

editUser(user: User) {
  this.selectedUser = { ...user };  // clone = EDIT mode
  this.showUserForm = true;
  this.errorMessage = '';
}


  viewUser(user: User) {
    // TODO: Implement view user details
    console.log('View user:', user);
    this.successMessage = `Viewing user: ${user.username}`;
    setTimeout(() => this.successMessage = '', 3000);
  }



  confirmDelete(user: User) {
    this.userToDelete = user;
    this.showDeleteConfirm = true;
  }

  cancelDelete() {
    this.userToDelete = null;
    this.showDeleteConfirm = false;
  }

  deleteUser(id: number) {
    console.log('Deleting user:', id);
    this.adminService.deleteUser(id).subscribe({
      next: () => {
        console.log('User deleted successfully');
        this.successMessage = 'User deleted successfully';
        this.showDeleteConfirm = false;
        this.userToDelete = null;
        this.loadUsers();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        console.error('Error deleting user:', err);
        this.errorMessage = 'Failed to delete user: ' + (err.message || 'Unknown error');
        this.showDeleteConfirm = false;
        this.userToDelete = null;
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
  switchTab(tab: 'users' | 'courses') {
  this.activeTab = tab;
  this.errorMessage = '';
  this.successMessage = '';
}
 handleSaveUser(userData: User) {
  if (this.selectedUser?.uid) {
    this.adminService.updateUser(this.selectedUser.uid, userData).subscribe({
      next: () => {
        this.successMessage = 'User updated successfully!';
        this.showUserForm = false;
        this.loadUsers();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        console.error('Update error:', err);
        if (err.status === 400) {
          this.errorMessage = 'Invalid data: ' + (err.error?.message || 'Please check your input');
        } else if (err.status === 409) {
          this.errorMessage = 'User with this username or email already exists';
        } else if (err.status === 404) {
          this.errorMessage = 'User not found';
        } else {
          this.errorMessage = 'Failed to update user: ' + (err.error?.message || err.message || 'Unknown error');
        }
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  } else {
    this.adminService.createUser(userData).subscribe({
      next: () => {
        this.successMessage = 'User created successfully!';
        this.showUserForm = false;
        this.loadUsers();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        console.error('Create error:', err);
        if (err.status === 400) {
          this.errorMessage = 'Invalid data: ' + (err.error?.message || 'Please check your input');
        } else if (err.status === 409) {
          this.errorMessage = 'User with this username or email already exists';
        } else {
          this.errorMessage = 'Failed to create user: ' + (err.error?.message || err.message || 'Unknown error');
        }
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }
}

}
