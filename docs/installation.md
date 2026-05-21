# Gradle Installation

Fulcrum artifacts are published under the `it.raniero` group. Add the Laprassi public Maven repository, then depend on the module that matches your platform or integration layer.

## Kotlin DSL

```kotlin
repositories {
    mavenCentral()
    maven {
        name = "laprassi-public"
        url = uri("https://repository.laprassi.ovh/repository/maven-public/")
    }
}
```

Use the platform module for the server you target:

```kotlin
dependencies {
    implementation("it.raniero:fulcrum-spigot:<CURRENT-VERSION>")
}
```

```kotlin
dependencies {
    implementation("it.raniero:fulcrum-bungeecord:<CURRENT-VERSION>")
}
```

```kotlin
dependencies {
    implementation("it.raniero:fulcrum-velocity:<CURRENT-VERSION>")
}
```

For API-only compile-time access:

```kotlin
dependencies {
    compileOnly("it.raniero:fulcrum-API:<CURRENT-VERSION>")
}
```

For direct database integration:

```kotlin
dependencies {
    implementation("it.raniero:fulcrum-database:<CURRENT-VERSION>")
}
```

## Groovy DSL

```groovy
repositories {
    mavenCentral()
    maven {
        name = 'laprassi-public'
        url = uri('https://repository.laprassi.ovh/repository/maven-public/')
    }
}
```

```groovy
dependencies {
    implementation 'it.raniero:fulcrum-spigot:<CURRENT-VERSION>'
}
```

Use `fulcrum-bungeecord` or `fulcrum-velocity` instead of `fulcrum-spigot` when targeting those platforms.

## Available Artifacts

| Module | Dependency |
| --- | --- |
| API | `it.raniero:fulcrum-API:<CURRENT-VERSION>` |
| Database | `it.raniero:fulcrum-database:<CURRENT-VERSION>` |
| Spigot | `it.raniero:fulcrum-spigot:<CURRENT-VERSION>` |
| BungeeCord | `it.raniero:fulcrum-bungeecord:<CURRENT-VERSION>` |
| Velocity | `it.raniero:fulcrum-velocity:<CURRENT-VERSION>` |

## Version Notes

Snapshot builds may include a `-SNAPSHOT` suffix depending on the published branch, so use the exact version shown by the repository for development builds.

