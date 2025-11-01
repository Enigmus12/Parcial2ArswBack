package eci.edu.co.Parcial2ArswBack.service;

import org.springframework.stereotype.Service;

import eci.edu.co.Parcial2ArswBack.model.Move;
import eci.edu.co.Parcial2ArswBack.model.Room;
import eci.edu.co.Parcial2ArswBack.repository.RoomRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class GameService {

    private final RoomRepository roomRepository;

    public GameService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room createRoom(String name, String playerX) {
        if (name == null || name.trim().isEmpty()) {
            name = "room-" + UUID.randomUUID().toString().substring(0,8);
        }
        Room r = new Room();
        r.setName(name);
        r.setPlayerX(playerX);
        r.setBoard("         ");
        r.setNextTurn("X");
        return roomRepository.save(r);
    }

    public Room joinRoom(String name, String playerId) {
        Optional<Room> opt = roomRepository.findByName(name);
        if (!opt.isPresent()) return null;
        Room r = opt.get();
        if (r.getPlayerO() == null && (r.getPlayerX() == null || !r.getPlayerX().equals(playerId))) {
            r.setPlayerO(playerId);
        }
        return roomRepository.save(r);
    }

    public Room getRoomByName(String name) {
        return roomRepository.findByName(name).orElse(null);
    }

    public Room makeMove(String roomName, int index, String player) {
        Room r = getRoomByName(roomName);
        if (r == null) return null;
        if (!r.getNextTurn().equals(player)) return r; // no es su turno
        String board = r.getBoard();
        if (index < 0 || index >= 9) return r;
        if (board.charAt(index) != ' ') return r; // ocupado

        StringBuilder sb = new StringBuilder(board);
        sb.setCharAt(index, player.charAt(0));
        r.setBoard(sb.toString());

        Move m = new Move();
        m.setIndexPos(index);
        m.setPlayer(player);
        m.setSequenceIndex(r.getMoves().size());
        r.addMove(m);

        r.setNextTurn(player.equals("X") ? "O" : "X");
        return roomRepository.save(r);
    }

    public Room undo(String roomName) {
        Room r = getRoomByName(roomName);
        if (r == null) return null;
        if (r.getMoves().isEmpty()) return r;
        Move last = r.getMoves().get(r.getMoves().size() - 1);
        int pos = last.getIndexPos();
        StringBuilder sb = new StringBuilder(r.getBoard());
        sb.setCharAt(pos, ' ');
        r.setBoard(sb.toString());

        r.removeLastMove();
        r.setNextTurn(last.getPlayer());
        return roomRepository.save(r);
    }
}