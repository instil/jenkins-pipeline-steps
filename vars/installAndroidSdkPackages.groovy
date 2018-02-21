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
    def requiredPackages = [
            "platform-tools",
            "tools",
            "emulator",
            "extras;android;m2repository",
            "extras;google;m2repository",
            "extras;intel;Hardware_Accelerated_Execution_Manager"
    ]
    def packages = (requiredPackages + requestedPackages).collect { "'$it'" }.join(" ")

    echo("Installing Android SDK packages: $packages")
    sh "$ANDROID_HOME/tools/bin/sdkmanager $packages"

    echo("Accepting Android SDK licenses")
    sh "yes | $ANDROID_HOME/tools/bin/sdkmanager --licenses"

//    echo("Installing HAXM")
//    sh "$ANDROID_HOME/extras/intel/Hardware_Accelerated_Execution_Manager/silent_install.sh || true"
}

private def failBuildWithError(String message) {
    echo message
    throw new IllegalStateException(message)
}
