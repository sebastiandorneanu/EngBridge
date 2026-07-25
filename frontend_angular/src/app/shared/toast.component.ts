import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, ToastMessage } from './toast.service';

interface ActiveToast extends ToastMessage {
  id: number;
  visible: boolean;
}

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      <div *ngFor="let toast of toasts" 
           class="toast" 
           [class]="toast.type"
           [class.show]="toast.visible">
        <div class="toast-content">
          <span class="icon">{{ getIcon(toast.type) }}</span>
          <span class="message">{{ toast.message }}</span>
        </div>
        <div class="progress-bar"></div>
      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 10px;
      pointer-events: none;
    }
    .toast {
      pointer-events: auto;
      min-width: 300px;
      padding: 15px 20px;
      border-radius: 8px;
      background: white;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      display: flex;
      flex-direction: column;
      transform: translateX(120%);
      transition: transform 0.3s ease-in-out;
      overflow: hidden;
      border-left: 6px solid #ccc;
    }
    .toast.show {
      transform: translateX(0);
    }
    .toast.success { border-left-color: #2ecc71; }
    .toast.error { border-left-color: #e74c3c; }
    .toast.warning { border-left-color: #f1c40f; }
    .toast.info { border-left-color: #3498db; }

    .toast-content {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .icon { font-size: 1.2rem; }
    .message { font-weight: 500; color: #333; }

    .progress-bar {
      height: 3px;
      background: rgba(0,0,0,0.1);
      margin-top: 10px;
      width: 100%;
      animation: progress 3s linear forwards;
    }

    @keyframes progress {
      from { width: 100%; }
      to { width: 0%; }
    }
  `]
})
export class ToastComponent implements OnInit {
  toasts: ActiveToast[] = [];
  private nextId = 0;

  constructor(private toastService: ToastService) {}

  ngOnInit() {
    this.toastService.toastState$.subscribe(toast => {
      this.addToast(toast);
    });
  }

  addToast(toast: ToastMessage) {
    const id = this.nextId++;
    const activeToast: ActiveToast = { ...toast, id, visible: false };
    this.toasts.push(activeToast);
    
    // Trigger animation
    setTimeout(() => activeToast.visible = true, 10);

    // Remove after 3 seconds
    setTimeout(() => {
      activeToast.visible = false;
      setTimeout(() => {
        this.toasts = this.toasts.filter(t => t.id !== id);
      }, 300);
    }, 3000);
  }

  getIcon(type: string): string {
    switch (type) {
      case 'success': return '✅';
      case 'error': return '❌';
      case 'warning': return '⚠️';
      default: return 'ℹ️';
    }
  }
}
