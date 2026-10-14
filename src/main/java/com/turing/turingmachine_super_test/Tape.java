package com.turing.turingmachine_super_test;

import java.util.ArrayList;
import java.util.List;

public class Tape {

    private List<Character> tape;
    private int headPosition;
    private final char BLANK = '_';

    public Tape() {
        this.tape = new ArrayList<>();
        // Инициализация ленты с несколькими пустыми символами
        for (int i = 0; i < 10; i++) {
            tape.add(BLANK);
        }
        headPosition = 5; // Начальная позиция головки
    }

    /**
     * Инициализация ленты с входной строкой.
     */
    public void initialize(String input) {
        tape.clear();
        tape.add(BLANK); // Начальный символ
        for (char c : input.toCharArray()) {
            tape.add(c);
        }
        for (int i = 0; i < 10; i++) {
            tape.add(BLANK);
        }
        headPosition = 1; // Позиция головки на первом символе входной строки
    }

    /**
     * Чтение символа
     */
    public char read() {
        ensureTapeSize(headPosition);
        return tape.get(headPosition);
    }

    /**
     * Запись символа
     */
    public void write(char c) {
        ensureTapeSize(headPosition);
        tape.set(headPosition, c);
    }

    /**
     * Перемещение головки.
     */
    public void moveHead(char direction) {
        if (direction == 'R') {
            headPosition++;
        } else if (direction == 'L') {
            headPosition--;
            if (headPosition < 0) {
                tape.add(0, BLANK);
                headPosition = 0;
            }
        }
    }

    /**
     * Обеспечение достаточного размера ленты.
     */
    private void ensureTapeSize(int position) {
        while (position >= tape.size()) {
            tape.add(BLANK);
        }
    }

    public List<Character> getTape() {
        return tape;
    }

    public int getHeadPosition() {
        return headPosition;
    }
}
