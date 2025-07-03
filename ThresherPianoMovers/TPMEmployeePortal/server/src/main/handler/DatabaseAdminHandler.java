package main.handler;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import records.ClearResponse;
import records.ErrorResponse;
import service.ClearDatabaseService;
import spark.Response;

public class DatabaseAdminHandler {
    final private AuthDAO authDataAccess;
    final private UserDAO userDataAccess;
    final private GameDAO gameDataAccess;
    final private Response res;

    public DatabaseAdminHandler(AuthDAO authDataAccess, UserDAO userDataAccess, GameDAO gameDataAccess, Response res){
        this.authDataAccess = authDataAccess;
        this.userDataAccess = userDataAccess;
        this.gameDataAccess = gameDataAccess;
        this.res = res;
    }

    public Object deleteDB(){
        Gson serializer = new Gson();
        ClearDatabaseService service = new ClearDatabaseService(authDataAccess, userDataAccess, gameDataAccess);
        Object response = service.deleteDB();
        if(response instanceof ErrorResponse){
            ErrorResponse temp = (ErrorResponse) response;
            res.status(temp.code());
            return serializer.toJson(temp);
        }
        else{
            ClearResponse temp = (ClearResponse) response;
            res.status(200);
            return serializer.toJson(temp);
        }
    }

}
