package database;

import models.request.PostUpdateEventRequest;
import util.Configuration;

import java.sql.*;

public class DBClient {
    Connection conn;

    private void connect() throws SQLException {
        conn = DriverManager.getConnection(Configuration.DB_URL, Configuration.DB_USER, Configuration.DB_PASSWORD);
    }

    public PostUpdateEventRequest getEventFromDB(String id) throws SQLException {
        connect();

        String sql = "SELECT * FROM events WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        ResultSet rs = pstmt.executeQuery();


        rs.next();

        conn.close();

        return PostUpdateEventRequest.builder()
                .title(rs.getString("title"))
                .image(rs.getString("image"))
                .date(rs.getString("date"))
                .location(rs.getString("location"))
                .description(rs.getString("description"))
                .build();
    }

    public void deleteUserByEmail(String email) throws SQLException {
        connect();

        String sql = "DELETE FROM \"Users\" WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.executeUpdate();
        } finally {
            conn.close();
        }
    }

    public void deleteUserById(int userId) throws SQLException {
        connect();

        String sql = "DELETE FROM \"Users\" WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } finally {
            conn.close();
        }
    }


    public void deleteEventById(String id) throws SQLException {
        connect();

        String sql = "DELETE FROM events WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }

        conn.close();
    }

    public boolean doesEventExist(String id) throws SQLException {
        connect();

        String sql = "SELECT 1 FROM events WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } finally {
            conn.close();
        }

    }
}
