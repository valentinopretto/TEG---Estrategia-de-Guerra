export interface ChatMessage {
  id: string;
  playerId: string;
  timestamp: Date;
  text: string;
  recipientId?: string; // For private messages, null means for everyone
}
