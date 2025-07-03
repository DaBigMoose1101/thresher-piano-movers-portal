package main.handler;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.GameData;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import server.ConnectionManager;
import service.WebSocketService;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;

@WebSocket
public class WebSocketHandler {
    static AuthDAO authDataAccess;
    static GameDAO gameDataAccess;
    static ConnectionManager connections = new ConnectionManager();
    private Gson serializer = new Gson();
    private int gameId;
    private boolean check;

    public static void initialize(AuthDAO auth, GameDAO game){
        authDataAccess = auth;
        gameDataAccess = game;
    }
    @OnWebSocketMessage
    public void onMessage(Session session, String message){
        WebSocketService service = new WebSocketService(authDataAccess, gameDataAccess);
        UserGameCommand com = serializer.fromJson(message, UserGameCommand.class);
        gameId = com.getGameID();
        ServerMessage serverMessage;
        String user;
        GameData game;
        try {
            switch (com.getCommandType()) {
                case CONNECT:
                    serverMessage = service.connectPlayer(session, com);
                    user = service.getUser();
                    if(serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME){
                        game = ((LoadGameMessage) serverMessage).getGame();
                        connections.addConnection(gameId, session);
                        String position = "observer";
                        if(game.whiteUsername()!= null && game.whiteUsername().equals(user)){
                            position = "white";
                        }
                        else if(game.blackUsername()!= null &&game.blackUsername().equals(user)){
                            position = "black";
                        }
                        NotificationMessage note = new NotificationMessage(user + " joined the game as " + position);
                        notifyConnections(note, session, gameId);
                        sendMessage(session, serverMessage);
                    }
                    else{
                        sendMessage(session, serverMessage);
                    }
                    break;
                case LEAVE:
                    serverMessage = service.leave(session, com);
                    if(serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION){
                        connections.removeSession(gameId, session);
                        notifyConnections(serverMessage, session, gameId);
                    }
                    else{
                        sendMessage(session, serverMessage);
                    }
                    break;
                case MAKE_MOVE:
                    MakeMoveCommand comm = new Gson().fromJson(message, MakeMoveCommand.class);
                    serverMessage = service.makeMove(session, comm);
                    user = service.getUser();
                    ChessMove move = comm.getMove();
                    if(serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME){
                        GameData g = service.getGame();
                        NotificationMessage note = getNotification(user, move, g);
                        notifyConnections(serverMessage, session, gameId);
                        notifyConnections(note, session, gameId);
                        sendMessage(session, serverMessage);
                        if(check){
                            sendMessage(session, note);
                        }
                        check = false;

                    }
                    else{
                        sendMessage(session, serverMessage);
                    }
                    break;
                case RESIGN:
                    serverMessage = service.resign(session, com);
                    if(serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION){
                        notifyConnections(serverMessage, session, gameId);
                        sendMessage(session, serverMessage);
                    }
                    else{
                        sendMessage(session, serverMessage);
                    }
                    break;
                default:
                    serverMessage = new ErrorMessage("Error: Invalid request");
                    sendMessage(session, serverMessage);
            }
        }
         catch (Exception e) {
            ErrorMessage err = new ErrorMessage(e.getMessage());
            try{
                sendMessage(session, err);
            }
            catch (IOException ex) {
                throw new RuntimeException(ex.getMessage());
            }
        }
    }

    @OnWebSocketConnect
     public void onConnect(Session session) {
        // Handle new WebSocket connections
        System.out.println("Client connected: " + session.getRemoteAddress());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Connection closed: " + reason);
        connections.removeSession(gameId, session);
    }

    private void sendMessage(Session session, ServerMessage message) throws IOException {
        String response = serializer.toJson(message);
        session.getRemote().sendString(response);
    }

  private void notifyConnections(ServerMessage message,
                                  Session session, int gameId) throws IOException {
        ArrayList<Session> sessions = connections.getSessions(gameId);
        if(sessions != null ) {
            for (Session ses : sessions) {
                if (ses != session) {
                    sendMessage(ses, message);
                }
            }
        }
    }

    private String parseMove(ChessMove move){
        String res = " moved ";
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        res += start.getRow() + convert(start.getColumn()) + " to " + end.getRow() + convert(end.getColumn());
        return res;
    }

    private String convert(int i) {
        return switch (i) {
            case 1 -> "a";
            case 2 -> "b";
            case 3 -> "c";
            case 4 -> "d";
            case 5 -> "e";
            case 6 -> "f";
            case 7 -> "g";
            case 8 -> "h";
            default -> "err";
        };
    }

    private NotificationMessage isInCheck(String user, ChessMove move, GameData game) {
        NotificationMessage note = null;
        check = false;
        if(game.game().isInCheck(ChessGame.TeamColor.WHITE)){
            note = new NotificationMessage(game.whiteUsername() + " is in check:("+user+parseMove(move)+")");
            check = true;
        }
        else if(game.game().isInCheck(ChessGame.TeamColor.BLACK)){
            note = new NotificationMessage(game.blackUsername() + " is in check:("+user+parseMove(move)+")");
            check = true;
        }
        return note;
    }

    private NotificationMessage isInCheckmate(String user, ChessMove move, GameData game){
        NotificationMessage note = null;
        check = false;
        if(game.game().isInCheckmate(ChessGame.TeamColor.WHITE)){
            note = new NotificationMessage(game.whiteUsername() + " is in checkmate:("+user+parseMove(move)+")");
            check = true;
        }
        else if(game.game().isInCheckmate(ChessGame.TeamColor.BLACK)){
            note = new NotificationMessage(game.blackUsername() + " is in checkmate:("+user+parseMove(move)+")");
            check = true;
        }
        return note;
    }

    private NotificationMessage isInStalemate(String user, ChessMove move, GameData game){
        NotificationMessage note = null;
        check = false;
        if(game.game().isInStalemate(ChessGame.TeamColor.WHITE)){
            note = new NotificationMessage(game.whiteUsername() + " is in stalemate:("+user+parseMove(move)+")");
            check = true;
        }
        else if(game.game().isInStalemate(ChessGame.TeamColor.BLACK)){
            note = new NotificationMessage(game.blackUsername() + " is in stalemate:("+user+parseMove(move)+")");
            check = true;
        }
        return note;
    }

    private NotificationMessage getNotification(String user, ChessMove move, GameData game){
        NotificationMessage checkmate = isInCheckmate(user, move, game);
        NotificationMessage isCheck = isInCheck(user, move, game);
        NotificationMessage stalemate = isInStalemate(user, move, game);
        if(checkmate != null){
            return checkmate;
        }
        if(isCheck != null){
            return isCheck;
        }
        if(stalemate != null){
            return stalemate;
        }
        return new NotificationMessage(user+parseMove(move));
    }

}
