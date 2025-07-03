package main.dataaccess;

import main.model.AuthData;

public interface AuthDAO {
    public void addAuthToken(AuthData token) throws DataAccessException;
    public AuthData getAuthToken(String token) throws DataAccessException;
    public void deleteAuthToken(AuthData token) throws DataAccessException;
    public void deleteDB() throws DataAccessException;
    public int getDataBaseSize() throws DataAccessException;
}
