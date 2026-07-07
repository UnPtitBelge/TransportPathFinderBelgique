package com.ulb.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GTFSLoader {
    private static final Logger logger = LoggerFactory.getLogger(GTFSLoader.class);
    private static final Path GTFS_DIR = Path.of("data/GTFS");

    public static CSVParser open(String filename) throws IOException {
        Path file = GTFS_DIR.resolve(filename);

        if (!Files.exists(file)) {
            logger.error("Fichier GTFS introuvable : {}", file.toAbsolutePath());
            throw new IOException("Fichier GTFS introuvable : " + file.toAbsolutePath());
        }

        logger.debug("Ouverture de {}", file);

        return CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .setIgnoreEmptyLines(true)
                .build()
                .parse(stripBOM(file));
    }

    /** Supprime le BOM UTF-8 (\uFEFF) si présent en début de fichier */
    private static Reader stripBOM(Path file) throws IOException {
        BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
        reader.mark(1);
        if (reader.read() != '\uFEFF') {
            reader.reset(); // pas de BOM → on revient au début
        }
        return reader;
    }
}
