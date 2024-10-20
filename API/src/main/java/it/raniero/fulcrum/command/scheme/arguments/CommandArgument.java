package it.raniero.fulcrum.command.scheme.arguments;

import lombok.Builder;

@Builder
public record CommandArgument(String name, Class<?> type, boolean required, String description) {

}
