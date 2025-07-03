package main.service;

import dataaccess.*;
import records.ClearResponse;

public class ClearDatabaseService {
    final private AuthDAO authDataAccess;
    final private UserDAO userDataAccess;
    final private GameDAO gameDataAccess;

    public ClearDatabaseService(AuthDAO authDataAccess, UserDAO userDataAccess, GameDAO gameDataAccess){
        this.authDataAccess = authDataAccess;
        this.userDataAccess = userDataAccess;
        this.gameDataAccess = gameDataAccess;
    }

    public Object deleteDB(){
        try{
        authDataAccess.deleteDB();
        userDataAccess.deleteDB();
        gameDataAccess.deleteDB();
        return new ClearResponse(null);
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
