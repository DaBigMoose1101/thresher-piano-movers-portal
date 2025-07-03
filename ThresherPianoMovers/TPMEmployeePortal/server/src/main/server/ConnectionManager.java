package main.server;


import org.eclipse.jetty.websocket.api.Session;

import java.util.ArrayList;
import java.util.HashMap;

public class ConnectionManager {
    private final HashMap<Integer, ArrayList<Session>> connections;

    public ConnectionManager(){
        connections = new HashMap<>();
    }

    public void addConnection(int gameId, Session session){
        ArrayList<Session> sessions = getSessions(gameId);
        if(sessions == null){
            sessions = new ArrayList<>();
        }
        sessions.add(session);
        connections.put(gameId, sessions);
    }

    public ArrayList<Session> getSessions(int gameId){
        return connections.get(gameId);
    }

    public void removeSession(int gameId, Session session){
        ArrayList<Session> sessions = getSessions(gameId);
        if(session != null && !sessions.isEmpty()) {
            sessions.remove(session);
            if(sessions.isEmpty()) {
                connections.remove(gameId, sessions);
            }
        }
    }
}
