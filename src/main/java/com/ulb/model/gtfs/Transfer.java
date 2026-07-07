package com.ulb.model.gtfs;

/** Correspondance à pied entre deux arrêts physiquement proches. */
public record Transfer(String fromStopId, String toStopId, int walkSeconds) {
}
