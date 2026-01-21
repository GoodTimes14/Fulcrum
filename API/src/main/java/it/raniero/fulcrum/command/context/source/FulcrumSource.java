package it.raniero.fulcrum.command.context.source;

public interface FulcrumSource {

    Object getSourceObject();

    void sendMessage(String text);

    SourceType sourceType();

    boolean hasPermission(String permissionNode);
}
