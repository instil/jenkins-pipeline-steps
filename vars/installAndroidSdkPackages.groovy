def call(List<String> packages = []) {
    if (env.ANDROID_HOME == null) {
        failBuildWithError("ANDROID_HOME not defined, cannot install SDK packages")
    }

    if (packages.size() <= 0) {
        scanWorkspaceForPackagesInBuildScripts()
    } else {
        installSdkPackages(packages)
    }
}

private def scanWorkspaceForPackagesInBuildScripts() {
    // TODO: Add support for Xamarin
    if (isGradleBuild()) {
        installSdkPackagesForGradleBuild()
    }
}

private def isGradleBuild() {
    return sh(script: "find . -name build.gradle | wc -l", returnStdout: true).toInteger() > 0
}

private def installSdkPackagesForGradleBuild() {
    def compileSdkVersions = sh(script: "find . -name build.gradle | xargs grep compileSdkVersion | grep -oE [0-9]+ || true", returnStdout: true)
            .split()
            .toList()
            .collect { "platforms;android-$it" }
    def buildToolsVersions = sh(script: "find . -name build.gradle | xargs grep buildToolsVersion | grep -oE [0-9]+.[0-9]+.[0-9]+ || true", returnStdout: true)
            .split()
            .toList()
            .collect { "build-tools;$it" }

    installSdkPackages(compileSdkVersions + buildToolsVersions)
}

private def installSdkPackages(List<String> requestedPackages) {
    def packages = requestedPackages.collect { "'$it'" }.join(" ")

    echo("Installing Android SDK packages: $packages")
    sh "/opt/homebrew/bin/sdkmanager $packages"

    echo("Accepting Android SDK licenses")
    sh "yes | /opt/homebrew/bin/sdkmanager --licenses"
}

private def failBuildWithError(String message) {
    echo message
    throw new IllegalStateException(message)
}
