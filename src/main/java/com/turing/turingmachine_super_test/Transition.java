package com.turing.turingmachine_super_test;

public class Transition {

    private char readSymbol;
    private char writeSymbol;
    private char direction; // 'L' или 'R'
    private int nextState;

    public Transition(char readSymbol, char writeSymbol, char direction, int nextState) {
        this.readSymbol = readSymbol;
        this.writeSymbol = writeSymbol;
        this.direction = direction;
        this.nextState = nextState;
    }

    public char getReadSymbol() {
        return readSymbol;
    }

    public char getWriteSymbol() {
        return writeSymbol;
    }

    public char getDirection() {
        return direction;
    }

    public int getNextState() {
        return nextState;
    }
}
