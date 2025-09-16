package it.raniero.fulcrum.utils;

import it.raniero.fulcrum.database.properties.DatabaseProperties;

public record StartupProperties(DatabaseProperties databaseProperties, RedisProperties redisProperties) {}
