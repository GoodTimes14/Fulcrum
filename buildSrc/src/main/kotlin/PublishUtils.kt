import java.io.ByteArrayOutputStream

fun getGitBranch(): String {
    val stdout = ByteArrayOutputStream()


    ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
        .redirectErrorStream(true)
        .start()
        .apply { waitFor() }
        .inputStream
        .use { stdout.writeBytes(it.readAllBytes()) }


    return stdout.toString().trim()
}

//Credits to Grim for these methods
fun getGitHash(): String {
    val stdout = ByteArrayOutputStream()

    ProcessBuilder("git", "rev-parse", "--short", "HEAD")
        .redirectErrorStream(true)
        .start()
        .apply { waitFor() }
        .inputStream
        .use { stdout.writeBytes(it.readAllBytes()) }

    return stdout.toString().trim()
}

fun isSnapshot(): Boolean {
    val currentBranch = getGitBranch()
    return currentBranch != "master";
}


fun correctVersion(rootVersion: String): String? {
    val suffix: String = if (isSnapshot()) "-SNAPSHOT" else ""
    val currentVersion: String = rootVersion;

    return if(currentVersion.endsWith(suffix)) currentVersion else currentVersion + suffix
}

fun correctPublishUrl(baseUrl: String): String {
    return baseUrl + (if (isSnapshot())  "maven-snapshots" else "maven-releases") + "/"
}
