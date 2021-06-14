def call(List<String> packages = []) {
    checkSdkInstallation()

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
    echo "Listing packages installed on system"
    String listInstalled = sh(
            script: "$ANDROID_CMDLINE_TOOLS_ROOT/bin/sdkmanager --list_installed",
            returnStdout: true
    ).trim()

    List<String> installedPackages = parseInstalledPackages(listInstalled)

    def packages = requestedPackages.findAll {
        !installedPackages.contains(it)
    }.collect { "'$it'" }.join(" ")

    if (packages.size() == 0) {
        echo "Packackes $requestedPackages are already installed; skipping..."
        return
    }

    echo("Installing Android SDK packages: $packages")
    sh "$ANDROID_CMDLINE_TOOLS_ROOT/bin/sdkmanager $packages"

    echo("Accepting Android SDK licenses")
    sh "yes | $ANDROID_CMDLINE_TOOLS_ROOT/bin/sdkmanager --licenses"
}

private def failBuildWithError(String message) {
    echo message
    throw new IllegalStateException(message)
}

@NonCPS
private List<String> parseInstalledPackages(String sdkmanagerResponse) {
    def result = []
    return sdkmanagerResponse.splitEachLine("\\|") {
        if(it.size() >= 0) result.add(it[0].trim())
        result
    }
}
