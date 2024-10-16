import java.util.*;

class Graph {

    private final Map<Character, List<Vertex>> vertices;

    public Graph() {
        this.vertices = new HashMap<>();
    }

    public void addVertex(Character id, List<Vertex> neighbors) {
        this.vertices.put(id, neighbors);
    }

    public Map<Character, List<Vertex>> getVertices() {
        return vertices;
    }
}
