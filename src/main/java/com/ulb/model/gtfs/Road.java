package com.ulb.model.gtfs;

import com.ulb.model.Transport;

public record Road(String id, String number, String name, Transport type) {
}
