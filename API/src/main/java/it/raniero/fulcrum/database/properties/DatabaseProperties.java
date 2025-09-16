package it.raniero.fulcrum.database.properties;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DatabaseProperties {

    private String name;
    private ConnectionType connectionType;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    public boolean isAuth() {
        return password != null;
    }
}
