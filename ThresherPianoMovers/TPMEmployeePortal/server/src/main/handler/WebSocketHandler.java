package main.handler;



import com.google.gson.Gson;
import main.dataaccess.AuthDAO;
import main.server.ConnectionManager;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.ArrayList;

@WebSocket
public class WebSocketHandler {
    static AuthDAO authDataAccess;
    static ConnectionManager connections = new ConnectionManager();
    private Gson serializer = new Gson();
    private int gameId;
    private boolean check;

    public static void initialize(AuthDAO auth){
        authDataAccess = auth;

    }
    @OnWebSocketMessage
    public void onMessage(Session session, String message){

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
