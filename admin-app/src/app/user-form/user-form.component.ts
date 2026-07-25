import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { User } from '../services/admin.service';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.css']
})
export class UserFormComponent implements OnChanges {
  @Input() isVisible = false;
  @Input() user: User | null = null;
  @Output() onClose = new EventEmitter<void>();
  @Output() onSave = new EventEmitter<User>();

  userForm: FormGroup;
  isEditMode = false;

  constructor(private fb: FormBuilder) {
    this.userForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      role: ['STUDENT', Validators.required]
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['user'] || changes['isVisible']) {
      this.isEditMode = !!this.user;

      if (this.isEditMode && this.user) {
        this.userForm.patchValue({
          username: this.user.username,
          email: this.user.email,
          password: '',
          role: this.user.role
        });

        this.userForm.get('username')?.clearValidators();
        this.userForm.get('email')?.clearValidators();
        this.userForm.get('password')?.clearValidators();

        this.userForm.get('email')?.setValidators([Validators.email]);

        this.userForm.get('username')?.updateValueAndValidity();
        this.userForm.get('email')?.updateValueAndValidity();
        this.userForm.get('password')?.updateValueAndValidity();
      } else {
        this.userForm.reset({
          username: '',
          email: '',
          password: '',
          role: 'STUDENT'
        });

        this.userForm.get('username')?.setValidators([Validators.required]);
        this.userForm.get('email')?.setValidators([Validators.required, Validators.email]);
        this.userForm.get('password')?.setValidators([Validators.required]);

        this.userForm.get('username')?.updateValueAndValidity();
        this.userForm.get('email')?.updateValueAndValidity();
        this.userForm.get('password')?.updateValueAndValidity();
      }
    }
  }

  closeModal() {
    this.userForm.reset();
    this.onClose.emit();
  }

  saveUser() {
    if (this.userForm.valid || this.isEditMode) {
      const formValue = this.userForm.value;
      const userData: any = {};

      if (formValue.username && formValue.username.trim()) {
        userData.username = formValue.username;
      }

      if (formValue.email && formValue.email.trim()) {
        userData.email = formValue.email;
      }

      if (formValue.password && formValue.password.trim()) {
        userData.password = formValue.password;
      }

      if (formValue.role) {
        userData.role = formValue.role;
      }

      this.onSave.emit(userData);
      this.userForm.reset();
    }
  }

  get title(): string {
    return this.isEditMode ? 'Edit User' : 'Create New User';
  }

  get submitButtonText(): string {
    return this.isEditMode ? 'Update User' : 'Create User';
  }
}
