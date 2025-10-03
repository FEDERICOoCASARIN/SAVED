# Chat API Endpoints for Company-Admin Communication

This document outlines the API endpoints needed to support the updated chat functionality where companies only have one chat with the admin.

## Endpoints

### For Companies (COMPANY role)

#### Get Admin Chat
- **Endpoint**: `GET /api/chats/admin`
- **Description**: Gets the chat overview for the company's conversation with admin
- **Authentication**: Company user only
- **Response**: Single chat object with admin
```json
{
  "participant": "Admin",
  "lastMsg": {
    "sender": "Admin",
    "message": "How can I help you today?"
  },
  "unread": "message_id_or_null",
  "time": "10:30 AM"
}
```

#### Get Admin Chat Messages
- **Endpoint**: `GET /api/chats/admin/messages`
- **Description**: Gets all messages in the company's conversation with admin
- **Authentication**: Company user only
- **Response**: Array of message objects
```json
[
  {
    "id": "msg_1",
    "sender": "Admin",
    "message": "Hello! How can I assist you?",
    "date": "2025-06-27",
    "time": "10:30"
  },
  {
    "id": "msg_2",
    "sender": "Company Name",
    "message": "I need help with my orders",
    "date": "2025-06-27",
    "time": "10:35"
  }
]
```

#### Send Message to Admin
- **Endpoint**: `POST /api/chats/admin/messages`
- **Description**: Company sends a message to admin
- **Authentication**: Company user only
- **Request Body**:
```json
{
  "sender": "Company Name",
  "message": "Message content",
  "date": "2025-06-27",
  "time": "10:35"
}
```

### For Admins (ADMIN role)

#### Get All Chats
- **Endpoint**: `GET /api/chats`
- **Description**: Gets overview of all chats (remains unchanged)
- **Authentication**: Admin user only
- **Response**: Array of chat objects with different companies

#### Get Messages for Specific Company
- **Endpoint**: `GET /api/chats/{companyId}/messages`
- **Description**: Gets all messages in conversation with specific company
- **Authentication**: Admin user only
- **Response**: Array of message objects

#### Send Message to Company
- **Endpoint**: `POST /api/chats/{companyId}/messages`
- **Description**: Admin sends a message to specific company
- **Authentication**: Admin user only
- **Request Body**:
```json
{
  "sender": "Admin",
  "message": "Message content",
  "date": "2025-06-27",
  "time": "10:40"
}
```

## Implementation Notes

1. **Authentication**: All endpoints should verify the user's role and only allow access to appropriate data
2. **Company Identification**: The backend should identify the company from the authenticated user's session/token
3. **Real-time Updates**: Consider implementing WebSocket support for real-time message delivery
4. **Message IDs**: Each message should have a unique ID for tracking read/unread status
5. **Participant Naming**: For companies, the participant is always "Admin". For admins, the participant should be the company name or identifier.

## Security Considerations

- Companies can only access their own chat with admin
- Admins can access all company chats
- Validate all message content before storing
- Implement rate limiting to prevent spam
- Sanitize message content to prevent XSS attacks
