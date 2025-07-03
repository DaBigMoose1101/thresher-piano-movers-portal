package main.dataaccess;

import model.UserData;

public interface UserDAO {
    public void addUser(UserData user) throws DataAccessException;
    public UserData getUser(String username) throws DataAccessException;
    public int getUserID(String userName) throws DataAccessException;
    public void updateUsername(String newUsername, int userID) throws DataAccessException;
    public void updatePassword(String newPassword, int userID) throws DataAccessException;
    public void updateEmail(String newEmail, int userID) throws DataAccessException;
    public boolean isAvailable(String attr) throws DataAccessException;
    public void deleteDB() throws DataAccessException;
    public int getDataBaseSize() throws DataAccessException;
}
