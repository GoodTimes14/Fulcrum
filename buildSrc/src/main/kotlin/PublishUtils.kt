import java.io.ByteArrayOutputStream

fun getGitBranch(): String {
    // GitHub Actions may be in detached HEAD; use env vars first
    System.getenv("GITHUB_HEAD_REF")?.takeIf { it.isNotEmpty() }?.let { return sanitizeVersionQualifier(it) }
    System.getenv("GITHUB_REF_NAME")?.takeIf { it.isNotEmpty() }?.let { return sanitizeVersionQualifier(it) }
    val githubRef = System.getenv("GITHUB_REF")
    if (githubRef != null && githubRef.startsWith("refs/heads/")) {
        return sanitizeVersionQualifier(githubRef.removePrefix("refs/heads/"))
    }

    val stdout = ByteArrayOutputStream()
    ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
        .redirectErrorStream(true)
        .start()
        .apply { waitFor() }
        .inputStream
        .use { stdout.writeBytes(it.readAllBytes()) }

    return sanitizeVersionQualifier(stdout.toString().trim())
}

private fun sanitizeVersionQualifier(value: String): String {
    return value
        .replace(Regex("[^A-Za-z0-9._-]+"), "-")
        .trim('-', '.', '_')
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
