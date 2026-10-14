package com.turing.turingmachine_super_test;

import javafx.application.Platform;

import java.util.*;

public class TuringMachine {

    private List<State> states;
    private Tape tape;
    private int currentState;
    private boolean isRunning;

    private String taskCondition;

    // Количество состояний
    private int stateCount;

    // Алфавит машины Тьюринга
    private Set<Character> alphabet = new HashSet<>();

    // Обработчики событий
    private Runnable onTapeChanged;
    private java.util.function.Consumer<String> onStatusChanged;

    public TuringMachine() {
        this.states = new ArrayList<>();
        this.tape = new Tape();
        this.currentState = 0;
        this.isRunning = false;
        this.stateCount = 0;
    }

    /**
     * Установка внешнего алфавита.
     */
    public void setAlphabet(List<String> alphabetList) {
        alphabet.clear();
        for (String symbol : alphabetList) {
            if (symbol.length() == 1) { // Проверяем, что это один символ
                alphabet.add(symbol.charAt(0));
            } else {
                throw new IllegalArgumentException("Каждый символ алфавита должен быть одним символом.");
            }
        }
        System.out.println("Алфавит установлен: " + alphabet);
    }

    /**
     * Установка условия задачи.
     */
    public void setTaskCondition(String condition) {
        this.taskCondition = condition;
    }

    /**
     * Добавление перехода для состояния.
     */
    public void addTransition(int stateIndex, Transition transition) {
        // Расширение списка состояний, если необходимо
        while (stateIndex >= states.size()) {
            states.add(new State(stateIndex));
            stateCount++;
        }
        states.get(stateIndex).addTransition(transition);
    }

    /**
     * Установка ленты с входной строкой.
     */
    public void setTape(String inputStr) {
        for (char symbol : inputStr.toCharArray()) {
            if (!alphabet.contains(symbol)) {
                throw new IllegalArgumentException("Символ не входит в алфавит.");
            }
        }
        tape.initialize(inputStr);
        notifyTapeChanged();
    }

    /**
     * Запуск машины
     */
    public void start() {
        isRunning = true;
        notifyStatusChanged("Запущена");
        // Запуск в отдельном потоке, чтобы не блокировать UI
        new Thread(this::run).start();
    }

    /**
     * Остановка машины
     */
    public void stop() {
        isRunning = false;
        notifyStatusChanged("Остановлена");
    }

    /**
     * Шаг вперёд.
     */
    public void stepForward() {
        if (!isRunning) {
            executeStep();
        }
    }

    /**
     * Основной цикл выполнения машины.
     */
    private void run() {
        while (isRunning && currentState != -1) {
            executeStep();
            try {
                Thread.sleep(500); // Задержка для визуализации
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (currentState == -1) {
            notifyStatusChanged("Завершена");
        }
    }

    /**
     * Выполнение одного шага
     */
    private void executeStep() {
        char currentSymbol = tape.read();
        Transition transition = findTransition(currentState, currentSymbol);
        if (transition != null) {
            tape.write(transition.getWriteSymbol());
            tape.moveHead(transition.getDirection());
            currentState = transition.getNextState();
            notifyTapeChanged();
            notifyStatusChanged("Выполняется: Q" + currentState);
        } else {
            // Нет перехода - завершение работы
            isRunning = false;
            currentState = -1;
            notifyStatusChanged("Завершена");
        }
    }

    /**
     * Поиск перехода для текущего состояния и символа.
     */
    private Transition findTransition(int state, char symbol) {
        if (state < 0 || state >= states.size()) {
            return null;
        }
        return states.get(state).getTransition(symbol);
    }

    /**
     * Получение объекта ленты.
     */
    public Tape getTape() {
        return tape;
    }

    /**
     * Получение количества состояний.
     */
    public int getStateCount() {
        return stateCount;
    }

    /**
     * Установка обработчика события изменения ленты.
     */
    public void setOnTapeChanged(Runnable handler) {
        this.onTapeChanged = handler;
    }

    /**
     * Динамическое изменение количества состояний.
     */
    public void setStateCount(int count) {
        // Добавляем недостающие состояния
        while (states.size() < count) {
            states.add(new State(states.size())); // Добавляем новое состояние с индексом
        }

        // Если количество состояний уменьшилось, обрезаем список
        while (states.size() > count) {
            states.remove(states.size() - 1); // Удаляем лишние состояния
        }

        stateCount = count; // Обновляем счётчик состояний
        System.out.println("Количество состояний обновлено: " + stateCount);
    }

    /**
     * Установка обработчика события изменения статуса.
     */
    public void setOnStatusChanged(java.util.function.Consumer<String> handler) {
        this.onStatusChanged = handler;
    }

    private void notifyTapeChanged() {
        if (onTapeChanged != null) {
            Platform.runLater(onTapeChanged);
        }
    }

    private void notifyStatusChanged(String status) {
        if (onStatusChanged != null) {
            Platform.runLater(() -> onStatusChanged.accept(status));
        }
    }

    /**
     * Обновление перехода из таблицы.
     */
    public void updateTransition(String symbol, int stateIndex, String transitionStr) {
        Transition transition = parseTransition(transitionStr);
        if (transition != null) {
            // Обновление в модели
            if (stateIndex < states.size()) {
                states.get(stateIndex).addTransition(transition);
            }
        }
    }

    /**
     * Парсинг строки перехода.
     * Формат: read,write,shift,nextState
     */
    private Transition parseTransition(String transitionStr) {
        String[] parts = transitionStr.split(",");
        if (parts.length != 4) {
            return null;
        }
        try {
            char read = parts[0].trim().charAt(0);
            char write = parts[1].trim().charAt(0);
            char shift = parts[2].trim().toUpperCase().charAt(0);
            int nextState = Integer.parseInt(parts[3].trim());
            return new Transition(read, write, shift, nextState);
        } catch (Exception e) {
            return null;
        }
    }
}

