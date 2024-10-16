import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.scene.text.Text;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DijkstraGUI extends Application {

    private Graph graph;
    private Map<Character, Circle> vertexCircles = new HashMap<>();
    private Map<String, Line> edgeLines = new HashMap<>();
    private Map<Character, Text> vertexLabels = new HashMap<>();
    private TextArea outputArea;

    @Override
    public void start(Stage primaryStage) {
        graph = createGraph();

        BorderPane root = new BorderPane();

        // Top: Controls
        HBox controls = createControls();
        root.setTop(controls);

        // Center: Graph Visualization
        Pane graphPane = createGraphPane();
        root.setCenter(graphPane);

        // Bottom: Output
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(100);
        root.setBottom(outputArea);

        Scene scene = new Scene(root, 800, 600);
        // Uncomment the following line if you add a styles.css file
        // scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setTitle("Dijkstra's Algorithm Visualization");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Creates the graph structure.
     */
    private Graph createGraph() {
        Graph g = new Graph();
        g.addVertex('A', Arrays.asList(new Vertex('B', 7), new Vertex('C', 8)));
        g.addVertex('B', Arrays.asList(new Vertex('A', 7), new Vertex('F', 2)));
        g.addVertex('C', Arrays.asList(new Vertex('A', 8), new Vertex('F', 6), new Vertex('G', 4)));
        g.addVertex('D', Arrays.asList(new Vertex('F', 8)));
        g.addVertex('E', Arrays.asList(new Vertex('H', 1)));
        g.addVertex('F', Arrays.asList(new Vertex('B', 2), new Vertex('C', 6), new Vertex('D', 8), new Vertex('G', 9), new Vertex('H', 3)));
        g.addVertex('G', Arrays.asList(new Vertex('C', 4), new Vertex('F', 9)));
        g.addVertex('H', Arrays.asList(new Vertex('E', 1), new Vertex('F', 3)));
        return g;
    }

    /**
     * Creates the top control panel with start and end inputs and a start button.
     */
    private HBox createControls() {
        HBox controls = new HBox(10);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER_LEFT);

        Label startLabel = new Label("Start:");
        ComboBox<Character> startCombo = new ComboBox<>();
        startCombo.getItems().addAll(graph.getVertices().keySet());
        startCombo.getSelectionModel().selectFirst();

        Label endLabel = new Label("End:");
        ComboBox<Character> endCombo = new ComboBox<>();
        endCombo.getItems().addAll(graph.getVertices().keySet());
        endCombo.getSelectionModel().selectLast();

        Button startButton = new Button("Start");
        startButton.setOnAction(e -> {
            Character start = startCombo.getValue();
            Character end = endCombo.getValue();
            if (start.equals(end)) {
                showAlert("Invalid Selection", "Start and end vertices must be different.");
                return;
            }
            clearHighlights();
            outputArea.clear();
            runDijkstra(start, end);
        });

        controls.getChildren().addAll(startLabel, startCombo, endLabel, endCombo, startButton);
        return controls;
    }

    /**
     * Creates the pane that visualizes the graph.
     */
    private Pane createGraphPane() {
        Pane pane = new Pane();
        pane.setStyle("-fx-background-color: #F0F0F0;");

        // Define positions for each vertex for simplicity
        Map<Character, double[]> positions = new HashMap<>();
        positions.put('A', new double[]{100, 100});
        positions.put('B', new double[]{100, 300});
        positions.put('C', new double[]{300, 100});
        positions.put('D', new double[]{500, 300});
        positions.put('E', new double[]{700, 300});
        positions.put('F', new double[]{300, 300});
        positions.put('G', new double[]{500, 100});
        positions.put('H', new double[]{700, 100});

        // Draw edges first
        for (Character from : graph.getVertices().keySet()) {
            for (Vertex neighbor : graph.getVertices().get(from)) {
                String edgeKey = from < neighbor.getId() ? from + "-" + neighbor.getId() : neighbor.getId() + "-" + from;
                if (!edgeLines.containsKey(edgeKey)) {
                    double[] fromPos = positions.get(from);
                    double[] toPos = positions.get(neighbor.getId());
                    Line line = new Line(fromPos[0], fromPos[1], toPos[0], toPos[1]);
                    line.setStrokeWidth(2);
                    line.setStroke(Color.GRAY);

                    // Add weight label
                    double midX = (fromPos[0] + toPos[0]) / 2;
                    double midY = (fromPos[1] + toPos[1]) / 2;
                    Text weightLabel = new Text(midX, midY, neighbor.getDistance().toString());
                    weightLabel.setStyle("-fx-font-size: 12px; -fx-fill: black;");

                    pane.getChildren().addAll(line, weightLabel);
                    edgeLines.put(edgeKey, line);
                }
            }
        }

        // Draw vertices
        for (Character vertex : graph.getVertices().keySet()) {
            double[] pos = positions.get(vertex);
            Circle circle = new Circle(pos[0], pos[1], 20, Color.LIGHTBLUE);
            circle.setStroke(Color.BLUE);
            circle.setStrokeWidth(2);

            Text label = new Text(pos[0] - 5, pos[1] + 5, vertex.toString());
            label.setStyle("-fx-font-size: 14px; -fx-fill: black;");

            vertexCircles.put(vertex, circle);
            vertexLabels.put(vertex, label);

            pane.getChildren().addAll(circle, label);
        }

        return pane;
    }

    /**
     * Runs Dijkstra's algorithm with visualization.
     */
    private void runDijkstra(Character start, Character end) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                // Initialize data structures
                Map<Character, Integer> distances = new HashMap<>();
                Map<Character, Character> previous = new HashMap<>();
                PriorityQueue<Vertex> queue = new PriorityQueue<>();

                for (Character vertex : graph.getVertices().keySet()) {
                    if (vertex.equals(start)) {
                        distances.put(vertex, 0);
                        queue.add(new Vertex(vertex, 0));
                        highlightVertex(vertex, Color.GREEN);
                        appendOutput("Starting at vertex " + vertex + " with distance 0.");
                    } else {
                        distances.put(vertex, Integer.MAX_VALUE);
                        queue.add(new Vertex(vertex, Integer.MAX_VALUE));
                        appendOutput("Vertex " + vertex + " initialized with distance ∞.");
                    }
                    previous.put(vertex, null);
                }

                while (!queue.isEmpty()) {
                    Vertex current = queue.poll();

                    if (current.getDistance() == Integer.MAX_VALUE) {
                        appendOutput("Vertex " + current.getId() + " is unreachable.");
                        break;
                    }

                    highlightVertex(current.getId(), Color.ORANGE);
                    appendOutput("Processing vertex " + current.getId() + " with current distance " + current.getDistance());
                    sleep(1000);

                    if (current.getId().equals(end)) {
                        appendOutput("Reached target vertex " + end + ".");
                        highlightVertex(current.getId(), Color.RED);
                        break;
                    }

                    for (Vertex neighbor : graph.getVertices().get(current.getId())) {
                        int alt = distances.get(current.getId()) + neighbor.getDistance();
                        appendOutput("Checking neighbor " + neighbor.getId() + " with edge weight " + neighbor.getDistance());

                        if (alt < distances.get(neighbor.getId())) {
                            distances.put(neighbor.getId(), alt);
                            previous.put(neighbor.getId(), current.getId());
                            queue.add(new Vertex(neighbor.getId(), alt));
                            appendOutput("Updated distance of vertex " + neighbor.getId() + " to " + alt);
                            highlightEdge(current.getId(), neighbor.getId(), Color.BLUE);
                            highlightVertex(neighbor.getId(), Color.YELLOW);
                            sleep(1000);
                        }
                    }
                    unhighlightVertex(current.getId());
                }

                // Reconstruct path
                List<Character> path = new ArrayList<>();
                Character step = end;
                if (previous.get(step) != null || step.equals(start)) {
                    while (step != null) {
                        path.add(step);
                        step = previous.get(step);
                    }
                    Collections.reverse(path);
                }

                if (!path.isEmpty()) {
                    appendOutput("Shortest path: " + path);
                    highlightPath(path);
                } else {
                    appendOutput("No path found from " + start + " to " + end + ".");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        executor.shutdown();
    }

    /**
     * Highlights a vertex with the specified color.
     */
    private void highlightVertex(Character vertex, Color color) {
        Platform.runLater(() -> {
            Circle circle = vertexCircles.get(vertex);
            if (circle != null) {
                circle.setFill(color);
            }
        });
    }

    /**
     * Unhighlights a vertex by resetting its color.
     */
    private void unhighlightVertex(Character vertex) {
        Platform.runLater(() -> {
            Circle circle = vertexCircles.get(vertex);
            if (circle != null) {
                circle.setFill(Color.LIGHTBLUE);
            }
        });
    }

    /**
     * Highlights an edge between two vertices with the specified color.
     */
    private void highlightEdge(Character from, Character to, Color color) {
        Platform.runLater(() -> {
            String edgeKey = from < to ? from + "-" + to : to + "-" + from;
            Line line = edgeLines.get(edgeKey);
            if (line != null) {
                line.setStroke(color);
            }
        });
    }

    /**
     * Resets all edges to their default color.
     */
    private void resetEdgeColors() {
        Platform.runLater(() -> {
            for (Line line : edgeLines.values()) {
                line.setStroke(Color.GRAY);
            }
        });
    }

    /**
     * Highlights the final path in red.
     */
    private void highlightPath(List<Character> path) {
        Platform.runLater(() -> {
            for (int i = 0; i < path.size() - 1; i++) {
                highlightEdge(path.get(i), path.get(i + 1), Color.RED);
            }
        });
    }

    /**
     * Clears all highlights from vertices and edges.
     */
    private void clearHighlights() {
        Platform.runLater(() -> {
            for (Circle circle : vertexCircles.values()) {
                circle.setFill(Color.LIGHTBLUE);
            }
            for (Line line : edgeLines.values()) {
                line.setStroke(Color.GRAY);
            }
        });
    }

    /**
     * Appends text to the output area.
     */
    private void appendOutput(String text) {
        Platform.runLater(() -> {
            outputArea.appendText("\n" + text);
        });
    }

    /**
     * Sleeps the thread for the specified milliseconds.
     */
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Handle interruption
        }
    }

    /**
     * Shows an alert dialog with the specified title and message.
     */
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
