package com.turing.turingmachine_super_test;

import java.util.HashMap;
import java.util.Map;

public class State {

    private int stateNumber;
    private Map<Character, Transition> transitions;

    public State(int stateNumber) {
        this.stateNumber = stateNumber;
        this.transitions = new HashMap<>();
    }

    /**
     * Добавление перехода.
     */
    public void addTransition(Transition transition) {
        transitions.put(transition.getReadSymbol(), transition);
    }

    /**
     * Получение перехода для символа.
     */
    public Transition getTransition(char symbol) {
        return transitions.get(symbol);
    }

    public int getStateNumber() {
        return stateNumber;
    }
}

