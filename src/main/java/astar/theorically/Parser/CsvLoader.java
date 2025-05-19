package astar.theorically.Parser;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class CsvLoader {
    public static <T> void loadFiles(List<String> paths, T map, CsvProcessor<T> processor) {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        settings.setLineSeparatorDetectionEnabled(false);
        settings.getFormat().setLineSeparator("\n");
        settings.getFormat().setDelimiter(',');
        settings.setMaxColumns(4);
        CsvParser parser = new CsvParser(settings);

        for (String path : paths) {
            try {
                parser.beginParsing(new FileReader(path));
                String[] row;
                while ((row = parser.parseNext()) != null) {
                    processor.process(row, map);
                }
                parser.stopParsing();
            } catch (IOException e) {
                System.err.println("Erreur de lecture : " + path);
                e.printStackTrace();
            }
        }
    }
}