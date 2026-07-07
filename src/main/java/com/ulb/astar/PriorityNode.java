package com.ulb.astar;

public record PriorityNode(StopNode node, double priority) implements Comparable<PriorityNode> {

    @Override
    public int compareTo(PriorityNode other) {
        return Double.compare(this.priority, other.priority);
    }
}
