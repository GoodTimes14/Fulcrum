package it.raniero.fulcrum.utils;

public record RedisProperties(String host,
                              int port,
                              String password) {


    public boolean isAuth() {
        return password != null;
    }

}
