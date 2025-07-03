package main.handler;



import com.google.gson.Gson;
import main.dataaccess.AuthDAO;
import main.server.ConnectionManager;
import main.server.ServerMessage;
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
}
