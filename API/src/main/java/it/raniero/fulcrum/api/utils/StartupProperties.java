package it.raniero.fulcrum.api.utils;

import it.raniero.fulcrum.api.database.properties.DatabaseProperties;

public record StartupProperties(DatabaseProperties databaseProperties, RedisProperties redisProperties) {}
