package astar.theorically.Parser;

public interface CsvProcessor<T> {
    void process(String[] row, T map);
}
