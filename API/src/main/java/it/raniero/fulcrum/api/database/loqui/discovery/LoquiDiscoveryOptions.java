package it.raniero.fulcrum.api.database.loqui.discovery;

public record LoquiDiscoveryOptions(long refreshInterval, long ttl, boolean aggressiveRetake) {


    public static LoquiDiscoveryOptions defaultOptions() {
        return new LoquiDiscoveryOptions(15,45,false);
    }

    public static boolean areOptionsValid(LoquiDiscoveryOptions options) {
        return options.ttl() >= (options.refreshInterval() * 2);
    }

}
