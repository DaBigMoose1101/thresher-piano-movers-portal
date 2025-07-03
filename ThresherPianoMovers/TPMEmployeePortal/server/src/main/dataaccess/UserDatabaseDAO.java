package main.dataaccess;

import dataaccess.DataAccessException;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public class UserDatabaseDAO implements UserDAO {
    public UserDatabaseDAO() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            var userTableStatement = """
                    CREATE TABLE IF NOT EXISTS users(
                    user_id INT NOT NULL AUTO_INCREMENT,
                    username VARCHAR(255) NOT NULL UNIQUE,
                    password_hash VARCHAR(1000) NOT NULL,
                    email VARCHAR(100) NOT NULL,
                    PRIMARY KEY (user_id)
                    )""";
            PreparedStatement statement = conn.prepareStatement(userTableStatement);
            statement.executeUpdate();

        } catch (SQLException | DataAccessException e) {
            String message = e.getMessage();
            throw new DataAccessException(message);
        }
    }

    @Override
    public void updateUsername(String newUsername, int userID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            try (var preparedStatement =
                         conn.prepareStatement("UPDATE users SET username = ? WHERE user_id = ?")) {
                preparedStatement.setString(1, newUsername);
                preparedStatement.setInt(2, userID);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException | DataAccessException e) {
            String message = e.getMessage();
            throw new DataAccessException(message);
        }
    }

    @Override
    public void updatePassword(String newPassword, int userID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            try (var preparedStatement =
                         conn.prepareStatement("UPDATE users SET password_hash = ? WHERE user_id = ?")) {
                preparedStatement.setString(1, newPassword);
                preparedStatement.setInt(2, userID);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException | DataAccessException e) {
            String message = e.getMessage();
            throw new DataAccessException(message);
        }
    }

    @Override
    public void updateEmail(String newEmail, int userID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            try (var preparedStatement =
                         conn.prepareStatement("UPDATE users SET email = ? WHERE user_id = ?")) {
                preparedStatement.setString(1, newEmail);
                preparedStatement.setInt(2, userID);
                preparedStatement.executeUpdate();
            }

        } catch (SQLException | DataAccessException e) {
            String message = e.getMessage();
            throw new DataAccessException(message);
        }

    }
    @Override
    public void addUser(UserData user) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            if(user.username().matches("[a-zA-Z0-9]+")) {
                try(PreparedStatement statement = conn.prepareStatement(
                        "INSERT INTO users(username, password_hash, email) VALUES(?, ?, ?)")) {
                    statement.setString(1, user.username());
                    statement.setString(2, user.password());
                    statement.setString(3, user.email());
                    statement.executeUpdate();
                }
            }

        } catch (SQLException | DataAccessException e) {
            String message = e.getMessage();
            throw new DataAccessException(message);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            try(PreparedStatement statement
                        = conn.prepareStatement("SELECT username, password_hash, email FROM users WHERE username = ?")){
                statement.setString(1, username);
                try(var queryResult = statement.executeQuery()){
                    if(queryResult.next()){
                        var password = queryResult.getString("password_hash");
                        var email = queryResult.getString("email");
                        return new UserData(username, password, email);
                    }
                    return null;
                }
            }

        } catch (SQLException | DataAccessException e) {
            String message = e.getMessage();
            throw new DataAccessException(message);
        }
    }

    public int getUserID(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (PreparedStatement statement
                         = conn.prepareStatement("SELECT user_id, username," +
                    " password_hash, email FROM users WHERE username = ?")) {
                statement.setString(1, username);
                try (var queryResult = statement.executeQuery()) {
                    if (queryResult.next()) {
                        return queryResult.getInt("user_id");
                    }
                }
            }
        }
        catch (SQLException | DataAccessException e) {
            String message = e.getMessage();
            throw new DataAccessException(message);
        }
        throw new DataAccessException("Error: Bad request");
    }
    public void deleteDB() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try(PreparedStatement statement = conn.prepareStatement("TRUNCATE TABLE users")) {
                statement.executeUpdate();
            }
        }
        catch (SQLException | DataAccessException e) {
            String message = e.getMessage();
            throw new DataAccessException(message);
        }
    }

    public boolean isAvailable(String attr) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try(PreparedStatement statement
                        = conn.prepareStatement("SELECT username, password_hash, email FROM users")) {
                var qRes = statement.executeQuery();
                while (qRes.next()) {

                    if (qRes.getString("username").equals(attr)
                            || qRes.getString("email").equals(attr)) {
                        return false;
                    }
                    if(BCrypt.checkpw(attr,qRes.getString("password_hash"))){
                        return false;
                    }
                }
            }

        }
        catch (SQLException | DataAccessException e) {
            String message = e.getMessage();
            throw new DataAccessException(message);
        }
        return true;
    }

    public int getDataBaseSize() throws DataAccessException {
        int res = 0;
        try (var conn = DatabaseManager.getConnection()) {
            try(PreparedStatement statement
                        = conn.prepareStatement("SELECT COUNT(*) FROM users")) {
                var rs = statement.executeQuery();
                if(rs.next()){
                    return rs.getInt(1);
                }
            }

        }
        catch (SQLException | DataAccessException e) {
            String message = e.getMessage();
            throw new DataAccessException(message);
        }
        return res;
    }
}
