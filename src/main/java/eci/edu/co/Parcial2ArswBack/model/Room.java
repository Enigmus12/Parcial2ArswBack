package eci.edu.co.Parcial2ArswBack.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "rooms")
public class Room {
    @Id
    private String id;

    private String name;
    private String board = "         ";
    private String playerX;
    private String playerO;
    private String nextTurn = "X";

    private List<Move> moves = new ArrayList<>();

    public Room() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBoard() { return board; }
    public void setBoard(String board) { this.board = board; }

    public String getPlayerX() { return playerX; }
    public void setPlayerX(String playerX) { this.playerX = playerX; }

    public String getPlayerO() { return playerO; }
    public void setPlayerO(String playerO) { this.playerO = playerO; }

    public String getNextTurn() { return nextTurn; }
    public void setNextTurn(String nextTurn) { this.nextTurn = nextTurn; }

    public List<Move> getMoves() { return moves; }
    public void setMoves(List<Move> moves) { this.moves = moves; }

    public void addMove(Move m) {
        moves.add(m);
    }

    public void removeLastMove() {
        if (!moves.isEmpty()) moves.remove(moves.size() - 1);
    }
}
