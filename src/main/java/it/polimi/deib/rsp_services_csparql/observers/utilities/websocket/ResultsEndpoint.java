package it.polimi.deib.rsp_services_csparql.observers.utilities.websocket;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

@ServerEndpoint(value = "/results")
public class ResultsEndpoint {

    public static final HashMap<String, ArrayList<Session>> connectedSession = new HashMap<>();

    @OnOpen
    public void open(Session session, EndpointConfig conf) {
        System.out.println("Client connected at " + Calendar.getInstance().getTime() + "\n" +
                "Session ID: " + session.getId());
        String sessionRequestURI = session.getRequestURI().toString();
        sessionRequestURI = sessionRequestURI.substring(0, sessionRequestURI.lastIndexOf("/"));
        sessionRequestURI = sessionRequestURI.substring(sessionRequestURI.lastIndexOf("/") + 1, sessionRequestURI.length());
        ArrayList<Session> tempList;
        if(connectedSession.containsKey(sessionRequestURI)){
            tempList = connectedSession.get(sessionRequestURI);
            tempList.add(session);
            connectedSession.put(sessionRequestURI, tempList);
        } else {
            tempList = new ArrayList<>();
            tempList.add(session);
            connectedSession.put(sessionRequestURI, tempList);
        }
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        return message;
    }

    @OnError
    public void error(Session session, Throwable error) {
    }

    @OnClose
    public void close(Session session, CloseReason reason) {

        String sessionRequestURI = session.getRequestURI().toString();
        sessionRequestURI = sessionRequestURI.substring(0, sessionRequestURI.lastIndexOf("/"));
        sessionRequestURI = sessionRequestURI.substring(sessionRequestURI.lastIndexOf("/") + 1, sessionRequestURI.length());

        ArrayList<Session> tempList = connectedSession.get(sessionRequestURI);
        tempList.remove(session);
        if(tempList.isEmpty())
            connectedSession.remove(sessionRequestURI);
        else
            connectedSession.put(sessionRequestURI, tempList);
    }
}
