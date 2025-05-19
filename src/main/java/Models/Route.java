package Models;

public record Route(String id, String name, String longName, String transportType) {
    /**
     * Constructor for Route (use of intern() for memory purposes).
     *
     * @param id            route ID
     * @param name          route name
     * @param longName      route long name
     * @param transportType route transport type
     */
    public Route(String id, String name, String longName, String transportType) {
        this.id = id;
        this.name = name;
        this.longName = longName;
        this.transportType = transportType.intern();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Route route = (Route) obj;
        return id.equals(route.id()) || name.equals(route.name());
    }
}
