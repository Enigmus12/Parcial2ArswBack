package eci.edu.co.Parcial2ArswBack.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eci.edu.co.Parcial2ArswBack.configuration.SpringContext;
import eci.edu.co.Parcial2ArswBack.model.Room;
import eci.edu.co.Parcial2ArswBack.service.GameService;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
@ServerEndpoint("/gameService")
public class GameEndpoint {
    private static final Logger logger = Logger.getLogger(GameEndpoint.class.getName());
    private static Queue<Session> sessions = new ConcurrentLinkedQueue<>();
    private final ObjectMapper mapper = new ObjectMapper();

    private GameService gameService() { return SpringContext.getBean(GameService.class); }

    @OnOpen
    public void open(Session session) {
        sessions.add(session);
        try {
            session.getBasicRemote().sendText("Connection established.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }
        logger.info("WS Open: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            ObjectNode node = (ObjectNode) mapper.readTree(message);
            String type = node.has("type") ? node.get("type").asText() : "";
            switch (type) {
                case "create":
                    handleCreate(node, session);
                    break;
                case "join":
                    handleJoin(node, session);
                    break;
                case "move":
                    handleMove(node, session);
                    break;
                case "undo":
                    handleUndo(node, session);
                    break;
                default:
                    sendError(session, "Unknown message type");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "onMessage error", e);
            sendError(session, "Invalid message format");
        }
    }

    private void handleCreate(ObjectNode node, Session session) throws IOException {
        String room = node.has("room") ? node.get("room").asText() : "";
        String player = node.has("player") ? node.get("player").asText("X") : "X";
        Room r = gameService().createRoom(room, player);
        session.getUserProperties().put("room", r.getName());
        broadcastRoomMessage(r, "created");
    }

    private void handleJoin(ObjectNode node, Session session) throws IOException {
        String room = node.has("room") ? node.get("room").asText() : "";
        String player = node.has("player") ? node.get("player").asText("O") : "O";
        Room r = gameService().joinRoom(room, player);
        if (r == null) {
            sendError(session, "Room not found: " + room);
            return;
        }
        session.getUserProperties().put("room", r.getName());
        broadcastRoomMessage(r, "joined");
    }

    private void handleMove(ObjectNode node, Session session) throws IOException {
        String room = node.has("room") ? node.get("room").asText() : "";
        int index = node.has("index") ? node.get("index").asInt(-1) : -1;
        String player = node.has("player") ? node.get("player").asText() : "";
        Room r = gameService().makeMove(room, index, player);
        if (r == null) {
            sendError(session, "Move failed");
            return;
        }
        broadcastRoomMessage(r, "update");
    }

    private void handleUndo(ObjectNode node, Session session) throws IOException {
        String room = node.has("room") ? node.get("room").asText() : "";
        Room r = gameService().undo(room);
        if (r == null) {
            sendError(session, "Undo failed");
            return;
        }
        broadcastRoomMessage(r, "update");
    }

    private void broadcastRoomMessage(Room r, String type) {
        try {
            ObjectNode payload = mapper.createObjectNode();
            payload.put("type", type);
            payload.set("room", mapper.valueToTree(roomToMap(r)));
            String text = mapper.writeValueAsString(payload);
            for (Session s : sessions) {
                if (s.isOpen()) {
                    // enviar solo a sesiones que están en la misma sala o a todos (aquí comprobamos)
                    Object roomProp = s.getUserProperties().get("room");
                    if (roomProp != null && roomProp.equals(r.getName())) {
                        s.getBasicRemote().sendText(text);
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    private void sendError(Session session, String msg) {
        try {
            ObjectNode p = mapper.createObjectNode();
            p.put("type", "error");
            p.put("message", msg);
            session.getBasicRemote().sendText(mapper.writeValueAsString(p));
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    @OnClose
    public void close(Session session) {
        sessions.remove(session);
        logger.info("WS Close: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        sessions.remove(session);
        logger.log(Level.SEVERE, "WS Error", thr);
    }

    private Map<String,Object> roomToMap(Room r) {
        Map<String,Object> m = new HashMap<>();
        m.put("id", r.getId());
        m.put("name", r.getName());
        m.put("board", r.getBoard());
        m.put("playerX", r.getPlayerX());
        m.put("playerO", r.getPlayerO());
        m.put("nextTurn", r.getNextTurn());
        List<Map<String,Object>> moves = new ArrayList<>();
        if (r.getMoves() != null) {
            r.getMoves().forEach(mv -> {
                Map<String,Object> mm = new HashMap<>();
                mm.put("index", mv.getIndexPos());
                mm.put("player", mv.getPlayer());
                mm.put("seq", mv.getSequenceIndex());
                mm.put("playedAt", mv.getPlayedAt().toString());
                moves.add(mm);
            });
        }
        m.put("moves", moves);
        return m;
    }
}
