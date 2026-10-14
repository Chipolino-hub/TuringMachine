package com.turing.turingmachine_super_test;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TapeCell {

    private int index;
    private char symbol;

    private StringProperty symbolProperty;

    public TapeCell(int index, char symbol) {
        this.index = index;
        this.symbol = symbol;
        this.symbolProperty = new SimpleStringProperty(String.valueOf(symbol));
    }

    public int getIndex() {
        return index;
    }

    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
        this.symbolProperty.set(String.valueOf(symbol));
    }

    public StringProperty symbolProperty() {
        return symbolProperty;
    }
}

