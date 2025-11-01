package eci.edu.co.Parcial2ArswBack.model;

import java.time.Instant;

public class Move {
    private int indexPos;
    private String player;
    private int sequenceIndex;
    private Instant playedAt = Instant.now();

    public Move() {}

    public Move(int indexPos, String player, int sequenceIndex) {
        this.indexPos = indexPos;
        this.player = player;
        this.sequenceIndex = sequenceIndex;
    }

    public int getIndexPos() { return indexPos; }
    public void setIndexPos(int indexPos) { this.indexPos = indexPos; }

    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }

    public int getSequenceIndex() { return sequenceIndex; }
    public void setSequenceIndex(int sequenceIndex) { this.sequenceIndex = sequenceIndex; }

    public Instant getPlayedAt() { return playedAt; }
    public void setPlayedAt(Instant playedAt) { this.playedAt = playedAt; }
}
