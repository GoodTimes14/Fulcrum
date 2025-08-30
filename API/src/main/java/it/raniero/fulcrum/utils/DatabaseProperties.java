package it.raniero.fulcrum.utils;

public record DatabaseProperties(
        String connectionType, String host, int port, String database, String username, String password) {

    private boolean isAuth() {
        return password != null;
    }
}
