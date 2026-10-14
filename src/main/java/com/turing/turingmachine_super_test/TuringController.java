package com.turing.turingmachine_super_test;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

public class TuringController {

    // Ссылка на модель машины Тьюринга
    private TuringMachine turingMachine;

    // FXML-элементы
    @FXML
    private TextArea taskConditionTextArea;

    @FXML
    private TextField alphabetField;

    @FXML
    private TableView<TransitionRow> transitionTable;

    @FXML
    private TableColumn<TransitionRow, String> symbolColumn;

    @FXML
    private TextArea commentTextArea;

    @FXML
    private ScrollPane tapeScrollPane;

    @FXML
    private HBox tapeContainer;

    @FXML
    private Label statusLabel;

    // Для динамического добавления столбцов Q0, Q1 и т.д.
    private List<TableColumn<TransitionRow, String>> stateColumns = new ArrayList<>();

    // Карта для хранения TransitionRow по символу
    private ObservableList<TransitionRow> transitionData = FXCollections.observableArrayList();

    // Видимые ячейки ленты
    private static final int VISIBLE_TAPE_CELLS = 20;

    @FXML
    public void initialize() {
        // Инициализация модели
        turingMachine = new TuringMachine();

        // Настройка таблицы переходов
        setupTransitionTable();

        transitionTable.setPlaceholder(new Label(""));

        // Рендер ленты
        renderTape();

        // Обновление ленты при изменении модели
        turingMachine.setOnTapeChanged(this::renderTape);

        // Обновление статуса
        turingMachine.setOnStatusChanged(this::updateStatus);
    }

    /**
     * Настройка таблицы переходов.
     */
    private void setupTransitionTable() {
        // Настройка первого столбца - символы алфавита
        symbolColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSymbol()));

        // Установка данных
        transitionTable.setItems(transitionData);
    }

    /**
     * Обновление таблицы переходов в реальном времени при вводе алфавита.
     */

    @FXML
    private void onAlphabetChanged() {
        String alphabetInput = alphabetField.getText().trim();

        // Проверка на пустой ввод
        if (alphabetInput.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Внешний алфавит не может быть пустым.");
            return;
        }

        // Разделяем алфавит на символы
        String[] symbols = alphabetInput.split("\\s+");
        Set<String> uniqueSymbols = new HashSet<>();

        // Проверяем символы на уникальность
        for (String symbol : symbols) {
            // Проверка на то, что символ является одиночным
            if (symbol.length() > 1) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Элемент алфавита должен быть только одним символом: '" + symbol + "'.");
                return; // Выход из метода, так как найдено слово
            }

            // Проверка на дублирующиеся символы
            if (!uniqueSymbols.add(symbol)) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Символ '" + symbol + "' уже введён.");
                return; // Выход из метода, так как найден повторяющийся символ
            }
        }

        // Сохраняем список старых символов
        Set<String> oldSymbols = new HashSet<>();
        for (TransitionRow row : transitionData) {
            oldSymbols.add(row.getSymbol());
        }

        // Добавляем новые символы в transitionData, не удаляя существующие
        for (String sym : uniqueSymbols) {
            if (transitionData.stream().noneMatch(row -> row.getSymbol().equals(sym))) {
                transitionData.add(new TransitionRow(sym));
            }
        }

        // Удаляем строки из таблицы, если символ больше не в алфавите
        Iterator<TransitionRow> iterator = transitionData.iterator();
        while (iterator.hasNext()) {
            TransitionRow row = iterator.next();
            if (!uniqueSymbols.contains(row.getSymbol())) {
                iterator.remove(); // Удаляем строку для символа, который больше не присутствует в алфавите
            }
        }

        // Очищаем ячейки в таблице для удаленных символов
        for (TransitionRow row : transitionData) {
            for (int i = 0; i < stateColumns.size(); i++) {
                String transition = row.getTransition(i);

                // Если переход содержит удаленный символ, очищаем эту ячейку
                if (transition != null && !transition.isEmpty()) {
                    String[] parts = transition.split(",");
                    if (parts.length == 4 && !uniqueSymbols.contains(String.valueOf(parts[1].charAt(0)))) {
                        row.setTransition(i, "");  // Очищаем ячейку, если переход содержит удаленный символ
                    }
                }
            }
        }

        // Получаем количество состояний из модели
        int stateCount = turingMachine.getStateCount();

        // Добавляем новые столбцы для состояний, если их еще нет
        for (int i = stateColumns.size(); i < stateCount; i++) {
            final int stateIndex = i;
            TableColumn<TransitionRow, String> stateColumn = new TableColumn<>("Q" + i);
            stateColumn.setCellValueFactory(data -> {
                String value = data.getValue().getTransition(stateIndex);
                return new SimpleStringProperty(value);
            });
            stateColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            stateColumn.setOnEditCommit(event -> {
                TransitionRow row = event.getRowValue();
                row.setTransition(stateIndex, event.getNewValue());
                // Обновление модели
                turingMachine.updateTransition(row.getSymbol(), stateIndex, event.getNewValue());
            });
            transitionTable.getColumns().add(stateColumn);
            stateColumns.add(stateColumn);
        }

        // Настройка редактирования таблицы
        transitionTable.setEditable(true);
    }


    @FXML
    private void onAddState() {
        // Получаем индекс для нового состояния (следующее состояние)
        int newStateIndex = stateColumns.size();

        // Создаём новый столбец для состояния Q[newStateIndex]
        TableColumn<TransitionRow, String> stateColumn = new TableColumn<>("Q" + newStateIndex);

        stateColumn.setPrefWidth(70);
        // Настраиваем отображение данных в этом столбце
        stateColumn.setCellValueFactory(data -> {
            String value = data.getValue().getTransition(newStateIndex);
            return new SimpleStringProperty(value);
        });

        // Настраиваем редактируемость столбца
        stateColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        stateColumn.setOnEditCommit(event -> {
            TransitionRow row = event.getRowValue();
            row.setTransition(newStateIndex, event.getNewValue());
            System.out.println("Сохранён переход: " + event.getNewValue() +
                    " для символа " + row.getSymbol() +
                    " в состоянии Q" + newStateIndex);
            turingMachine.updateTransition(row.getSymbol(), newStateIndex, event.getNewValue());
        });

        // Добавляем столбец в таблицу
        transitionTable.getColumns().add(stateColumn);
        stateColumns.add(stateColumn);

        // Увеличиваем количество состояний в машине
        turingMachine.setStateCount(stateColumns.size());
        System.out.println("Добавлено состояние Q" + newStateIndex);
    }

    /**
     * Обработка нажатия кнопки "Старт".
     */
    @FXML
    private void onStart() {
        try {
            // Считывание алфавита
            String alphabetInput = alphabetField.getText().trim();
            if (alphabetInput.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Внешний алфавит не может быть пустым.");
                return;
            }
            String[] symbols = alphabetInput.split("\\s+");
            Set<String> uniqueSymbols = new HashSet<>(Arrays.asList(symbols));
            if (uniqueSymbols.size() != symbols.length) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Символы алфавита должны быть уникальными.");
                return;
            }
            turingMachine.setAlphabet(Arrays.asList(symbols));

            // Считывание условия задачи
            String taskCondition = taskConditionTextArea.getText().trim();
            turingMachine.setTaskCondition(taskCondition);

            // Инициализация таблицы переходов
            onAlphabetChanged();

            // Считывание переходов из таблицы
            for (TransitionRow row : transitionData) {
                for (int i = 0; i < turingMachine.getStateCount(); i++) {
                    String transitionStr = row.getTransition(i);
                    if (transitionStr != null && !transitionStr.isEmpty()) {
                        // Обработка строки формата "abR1"
                        processTransitionString(transitionStr, row, i);
                    }
                }
            }

            // Считывание входной строки
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Входная строка");
            dialog.setHeaderText("Введите входную строку для машины Тьюринга:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(inputStr -> {
                turingMachine.setTape(inputStr);
                renderTape();
            });

            // Запуск машины
            turingMachine.start();
            statusLabel.setText("Статус: Запущена");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Произошла ошибка при запуске машины: " + e.getMessage());
        }
    }

    /**
     * Обработка нажатия кнопки "Стоп".
     */
    @FXML
    private void onStop() {
        turingMachine.stop();
        statusLabel.setText("Статус: Остановлена");
    }

    /**
     * Обработка нажатия кнопки "Шаг вперёд".
     */
    @FXML
    private void onStepForward() {
        turingMachine.stepForward();
        renderTape();
    }

    /**
     * Обработка нажатия кнопки "Прокрутка влево".
     */
    @FXML
    private void onScrollLeft() {
        // Прокрутка ScrollPane влево
        double hValue = tapeScrollPane.getHvalue();
        tapeScrollPane.setHvalue(Math.max(hValue - 0.1, 0));
    }

    /**
     * Обработка нажатия кнопки "Прокрутка вправо".
     */
    @FXML
    private void onScrollRight() {
        // Прокрутка ScrollPane вправо
        double hValue = tapeScrollPane.getHvalue();
        tapeScrollPane.setHvalue(Math.min(hValue + 0.1, 1));
    }

    /**
     * Рендер ленты на экране.
     */
    private void renderTape() {
        Platform.runLater(() -> {
            tapeContainer.getChildren().clear();

            List<Character> tapeSymbols = turingMachine.getTape().getTape();
            int headPosition = turingMachine.getTape().getHeadPosition();

            // Получаем ширину ScrollPane и вычисляем количество ячеек
            double containerWidth = tapeScrollPane.getViewportBounds().getWidth(); // Текущая ширина видимой области
            int cellWidth = 40; // Ширина одной ячейки
            int requiredCells = (int) Math.ceil(containerWidth / cellWidth); // Количество ячеек для полного заполнения

            for (int i = 0; i < requiredCells; i++) {
                VBox cellBox = new VBox();
                cellBox.setAlignment(javafx.geometry.Pos.CENTER);
                cellBox.setStyle("-fx-border-color: black; -fx-padding: 5; -fx-min-width: 40; -fx-max-width: 40;");

                // Устанавливаем индекс и символ ячейки
                Label indexLabel = new Label(String.valueOf(i));
                char symbol = (i < tapeSymbols.size()) ? tapeSymbols.get(i) : '_'; // Символ или пустая ячейка
                Label symbolLabel = new Label(String.valueOf(symbol));
                symbolLabel.setStyle("-fx-font-size: 16;");

                // Выделяем текущую ячейку с головкой
                if (i == headPosition) {
                    cellBox.setStyle("-fx-border-color: red; -fx-background-color: lightblue; -fx-padding: 5; -fx-min-width: 40; -fx-max-width: 40;");
                }

                cellBox.getChildren().addAll(indexLabel, symbolLabel);
                tapeContainer.getChildren().add(cellBox);
            }
        });
    }

    /**
     * Обновление статуса.
     */
    private void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText("Статус: " + status));
    }

    /**
     * Вспомогательный метод для отображения алертов.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Метод обработки строки перехода (формат: abR1)
     */
    private void processTransitionString(String transitionStr, TransitionRow row, int stateIndex) {
        if (transitionStr.length() != 4) {
            showAlert(Alert.AlertType.ERROR, "Ошибка", "Неверный формат перехода для символа " + row.getSymbol() + " и состояния Q" + stateIndex);
            return;
        }

        char read = transitionStr.charAt(0); // Первый символ - для чтения
        char write = transitionStr.charAt(1); // Второй символ - для записи
        char shift = transitionStr.charAt(2); // Третий символ - для направления
        int nextState = Character.getNumericValue(transitionStr.charAt(3)); // Четвёртый символ - номер состояния

        // Теперь добавляем переход в модель
        turingMachine.addTransition(stateIndex, new Transition(read, write, shift, nextState));
    }

    /**
     * Класс для строки таблицы переходов.
     */
    public static class TransitionRow {
        private final String symbol;
        private final Map<Integer, String> transitions;
        public TransitionRow(String symbol) {
            this.symbol = symbol;
            this.transitions = new HashMap<>();
        }

        public String getSymbol() {
            return symbol;
        }

        public String getTransition(int stateIndex) {
            return transitions.getOrDefault(stateIndex, "");
        }

        public void setTransition(int stateIndex, String transition) {
            transitions.put(stateIndex, transition);
        }
    }
}
