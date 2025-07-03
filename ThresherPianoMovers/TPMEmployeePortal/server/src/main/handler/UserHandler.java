package main.handler;

import com.google.gson.Gson;

import main.dataaccess.*;
import main.records.*;
import main.service.*;
import spark.Response;

public class UserHandler {
    final private UserDAO userDataAccess;
    final private AuthDAO authDataAccess;
    final private Response res;

    public UserHandler(UserDAO userDataAccess, AuthDAO authDataAccess, Response res){
        this.userDataAccess = userDataAccess;
        this.authDataAccess = authDataAccess;
        this.res = res;
    }

    public Object register(String body){
        Gson serializer = new Gson();
        RegisterRequest req = serializer.fromJson(body, RegisterRequest.class);
        UserService service = new UserService(userDataAccess, authDataAccess);
        Object response = service.registerUser(req);
        if(response instanceof ErrorResponse){
            ErrorResponse temp = (ErrorResponse) response;
            res.status(temp.code());
            return serializer.toJson(temp);
        }
        else{
            RegisterResponse temp = (RegisterResponse) response;
            res.status(200);
            return serializer.toJson(temp);
        }
    }

    public Object login(String body){
        Gson serializer = new Gson();
        LoginRequest req = serializer.fromJson(body, LoginRequest.class);
        UserService service = new UserService(userDataAccess, authDataAccess);
        Object response = service.login(req);
        if(response instanceof ErrorResponse){
            ErrorResponse temp = (ErrorResponse) response;
            res.status(temp.code());
            return serializer.toJson(temp);
        }
        else{
            LoginResponse temp = (LoginResponse) response;
            res.status(200);
            return serializer.toJson(temp);
        }

    }

    public Object logout(String header){
        Gson serializer = new Gson();
        LogoutRequest req = new LogoutRequest(header);
        UserService service = new UserService(userDataAccess, authDataAccess);
        Object response = service.logout(req);
        if(response instanceof ErrorResponse){
            ErrorResponse temp = (ErrorResponse) response;
            res.status(temp.code());
            return serializer.toJson(temp);
        }
        else{
            LogoutResponse temp = (LogoutResponse) response;
            res.status(200);
            return serializer.toJson(temp);
        }


    }
}
