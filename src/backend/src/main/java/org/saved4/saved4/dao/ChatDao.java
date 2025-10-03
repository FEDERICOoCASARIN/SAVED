package org.saved4.saved4.dao;

import jakarta.inject.Singleton;
import org.saved4.saved4.DBProvider;
import org.saved4.saved4.dto.Message;
import org.saved4.saved4.dto.chat.AdminChat;
import org.saved4.saved4.dto.chat.Chat;
import org.saved4.saved4.enums.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ChatDao {

    public String create(Role role, Chat chat) throws SQLException {
        String stmt = "INSERT INTO chats (company) VALUES (?)";

        Connection conn = DBProvider.getConnection(role);
        PreparedStatement preparedStmt = conn.prepareStatement(stmt);

        preparedStmt.setString(1, chat.getCompany());

        preparedStmt.executeUpdate();
        conn.commit();
        return chat.getCompany();

    }

    public Chat getByCompany(Role role, String dbRequester, String company) throws SQLException {
        String query = """
                 SELECT c.unread, m.message_id, m.time, m.sender, m.body\s
                 FROM chats c\s
                 JOIN users u ON c.company = u.username\s
                 LEFT JOIN messages m ON c.company = m.company\s
                 WHERE c.company = ? AND u.is_active = true\s
                 ORDER BY m.time DESC\s
                 LIMIT 1
                \s""";

        Connection conn = DBProvider.getConnection(role, dbRequester);
        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setString(1, company);

        ResultSet resultSet = preparedStmt.executeQuery();
        if (!resultSet.next()) return null;

        int unread = resultSet.getInt("unread");

        // Get last message if exists
        Message lastMsg = null;
        int messageId = resultSet.getInt("message_id");
        if (!resultSet.wasNull()) {
            Timestamp time = resultSet.getTimestamp("time");
            String sender = resultSet.getString("sender");
            String body = resultSet.getString("body");
            lastMsg = new Message(messageId, company, time, sender, body);
        }

        return new Chat(company, company, unread, lastMsg);
    }

    public List<AdminChat> list(Role role) throws SQLException {
        String query = """
                 SELECT c.company, c.unread, m.message_id, m.time, m.sender, m.body\s
                 FROM chats c\s
                 JOIN users u ON c.company = u.username\s
                 LEFT JOIN messages m ON c.company = m.company\s
                 WHERE u.is_active = TRUE\s
                 AND m.time = (
                     SELECT MAX(m2.time)\s
                     FROM messages m2\s
                     WHERE m2.company = c.company
                 )
                 ORDER BY m.time DESC
                \s""";

        Connection conn = DBProvider.getConnection(role);
        Statement stmt = conn.createStatement();

        List<AdminChat> chats = new ArrayList<>();
        ResultSet resultSet = stmt.executeQuery(query);
        while (resultSet.next()) {
            String company = resultSet.getString("company");
            int unread = resultSet.getInt("unread");

            // Get last message if exists
            Message lastMsg = null;
            int lastMessageId = resultSet.getInt("message_id");
            if (!resultSet.wasNull()) {
                Timestamp time = resultSet.getTimestamp("time");
                String sender = resultSet.getString("sender");
                String body = resultSet.getString("body");
                lastMsg = new Message(lastMessageId, company, time, sender, body);
            }

            chats.add(new AdminChat(company, unread, lastMsg));
        }
        return chats;
    }

    public boolean updateUnread(Role role, int company, int updatedUnread) throws SQLException {
        String query = "UPDATE chats c SET unread = ? FROM users u WHERE  c.company = ? AND c.company = u.username AND u.is_active = TRUE;";

        Connection conn = DBProvider.getConnection(role);
        PreparedStatement preparedStmt = conn.prepareStatement(query);

        preparedStmt.setInt(1, updatedUnread);
        preparedStmt.setInt(2, company);

        int affectedRows = preparedStmt.executeUpdate();
        conn.commit();
        return affectedRows > 0;
    }
}
