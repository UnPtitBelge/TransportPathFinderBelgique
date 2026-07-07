package com.ulb.model.gtfs;

import com.ulb.util.Position;

public record Stop(String id, String name, Position position) {
}
