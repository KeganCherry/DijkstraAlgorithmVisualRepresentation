class Vertex implements Comparable<Vertex> {

    private Character id;
    private Integer distance;

    public Vertex(Character id, Integer distance) {
        this.id = id;
        this.distance = distance;
    }

    public Character getId() {
        return id;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(Vertex o) {
        return this.distance.compareTo(o.distance);
    }
}
