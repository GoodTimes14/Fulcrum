package it.raniero.fulcrum.utils;

import it.raniero.fulcrum.command.scheme.arguments.CommandArgument;

public record RedisProperties(String host,
                              int port,
                              String password) {


    public boolean isAuth() {
        return password != null;
    }

}
