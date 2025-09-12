import { Component, Input, Output, EventEmitter, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-message-input',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './message-input.component.html',
  styleUrls: ['./message-input.component.css']
})
export class MessageInputComponent {
  @Input() maxLength: number = 150;
  @Input() placeholder: string = 'Escribe un mensaje...';
  @Input() disabled: boolean = false;

  @Output() onSendMessage = new EventEmitter<string>();

  @ViewChild('messageInput', { static: false }) messageInput!: ElementRef<HTMLInputElement>;

  messageText: string = '';

  onSubmit(event?: Event) {
    if (event) {
      event.preventDefault();
    }

    if (this.canSendMessage()) {
      const trimmedMessage = this.messageText.trim();
      this.onSendMessage.emit(trimmedMessage);
      this.clearInput();
    }
  }

  onKeyPress(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.onSubmit();
    }
  }



  private clearInput() {
    this.messageText = '';
    if (this.messageInput) {
      this.messageInput.nativeElement.focus();
    }
  }

  focusInput() {
    if (this.messageInput) {
      this.messageInput.nativeElement.focus();
    }
  }

  getRemainingChars(): number {
    return this.maxLength - this.messageText.length;
  }

  isNearLimit(): boolean {
    return this.getRemainingChars() < 50;
  }

  isOverLimit(): boolean {
    return this.messageText.length > this.maxLength;
  }

  canSendMessage(): boolean {
    return !this.disabled &&
      this.messageText.trim().length > 0 &&
      this.messageText.trim().length <= this.maxLength;
  }
}
