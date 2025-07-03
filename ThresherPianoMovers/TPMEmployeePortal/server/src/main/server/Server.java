package main.server;



import main.dataaccess.*;
import main.handler.*;

import spark.*;

public class Server {
    final private UserDAO userDataAccess;
    final private AuthDAO authDataAccess;
    public Server() {
        try {
            DatabaseManager.createDatabase();
            this.userDataAccess = new UserDatabaseDAO();
            this.authDataAccess = new AuthDatabaseDAO();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.webSocket("/ws", WebSocketHandler.class);
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.post("/storage", this::addItem);
        Spark.get("/storage", this::getItem);
        Spark.put("/storage", this::moveItem);
        Spark.delete("/storage", this::removeItem);
        Spark.delete("/db", this::clearServer);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object register(Request req, Response res) {
        UserHandler handler = new UserHandler(userDataAccess, authDataAccess, res);
        return handler.register(req.body());
    }

    private Object login(Request req, Response res){
        UserHandler handler = new UserHandler(userDataAccess, authDataAccess, res);
        return handler.login(req.body());
    }

    private Object logout(Request req, Response res){
        UserHandler handler = new UserHandler(userDataAccess, authDataAccess, res);
        return handler.logout(req.headers("authorization"));

    }

    private Object addItem(Request req, Response res){
        StorageHandler handler = new StorageHandler(authDataAccess, gameDataAccess, res);
        return handler.createGame(req.headers("authorization"), req.body());
    }

    private Object moveItem(Request req, Response res){
        GameHandler handler = new GameHandler(authDataAccess, gameDataAccess, res);
        return handler.joinGame(req.headers("authorization"), req.body());
    }

    private Object getItem(Request req, Response res){
        GameHandler handler = new GameHandler(authDataAccess, gameDataAccess, res);
        return handler.getGameList(req.headers("authorization"));
    }

    private Object removeItem(Request req, Response res){

        return res;
    }
    private Object clearServer(Request req, Response res){
        DatabaseAdminHandler handler = new DatabaseAdminHandler(authDataAccess, userDataAccess, gameDataAccess, res);
        return handler.deleteDB();
    }

}
