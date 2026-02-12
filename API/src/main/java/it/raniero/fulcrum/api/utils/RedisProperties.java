package it.raniero.fulcrum.api.utils;

public record RedisProperties(String host, int port, String password) {

    public boolean isAuth() {
        return password != null;
    }
}
