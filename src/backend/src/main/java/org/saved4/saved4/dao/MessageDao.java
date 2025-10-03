package org.saved4.saved4.dao;

import jakarta.inject.Singleton;
import org.saved4.saved4.DBProvider;
import org.saved4.saved4.dto.Message;
import org.saved4.saved4.enums.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class MessageDao {

    public Integer create(Message message, Role role) throws SQLException {
        String stmt = "INSERT INTO messages (company, time, body, sender) VALUES (?, ?, ?, ?)";

        Connection conn = DBProvider.getConnection(role, message.getSender());
        PreparedStatement preparedStmt = conn.prepareStatement(stmt, Statement.RETURN_GENERATED_KEYS);

        preparedStmt.setString(1, message.getCompany());
        preparedStmt.setTimestamp(2, message.getTime());
        preparedStmt.setString(3, message.getBody());
        preparedStmt.setString(4, message.getSender());

        preparedStmt.executeUpdate();
        conn.commit();
        ResultSet generatedKeys = preparedStmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getInt(1);
        } else {
            throw new SQLException("Creating user failed, no ID obtained.");
        }
    }

    public List<Message> list(String company, String requester, Role role) throws SQLException {
        String query = "SELECT * FROM messages WHERE company = ?";

        Connection conn = DBProvider.getConnection(role, requester);
        PreparedStatement preparedStmt = conn.prepareStatement(query);
        preparedStmt.setString(1, company);

        List<Message> messages = new ArrayList<>();
        ResultSet resultSet = preparedStmt.executeQuery();
        while (resultSet.next()) {
            int messageId = resultSet.getInt("message_id");
            Timestamp time = resultSet.getTimestamp("time");
            String sender = resultSet.getString("sender");
            String body = resultSet.getString("body");

            messages.add(new Message(messageId, company, time, sender, body));
        }
        return messages;
    }
}
