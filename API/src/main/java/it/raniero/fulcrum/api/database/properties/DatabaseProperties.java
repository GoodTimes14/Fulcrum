package it.raniero.fulcrum.api.database.properties;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DatabaseProperties {

    private String name;
    private boolean enabled;
    private ConnectionType connectionType;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    private boolean ssl;

    @Builder.Default
    private boolean verifyPeer = true;

    private boolean startTls;

    public boolean isAuth() {
        return password != null;
    }
}
