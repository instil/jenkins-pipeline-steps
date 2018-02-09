package co.instil.jenkins

import groovy.io.FileType

class AndroidSdkInstaller {

    private final requiredPackages = [
            "platform-tools",
            "tools",
            "emulator",
            "extras;android;m2repository",
            "extras;google;m2repository",
            "extras;intel;Hardware_Accelerated_Execution_Manager"
    ]

    void scanRepositoryForBuildScripts() {
        if (isGradleBuild()) {
            installSdkPackagesForGradleBuild()
        } else if (isXamarinBuild()) {
            installSdkPackagesForXamarinBuild()
        }
    }

    private boolean isGradleBuild() {
        def gradleScripts = new File(".").findAll { it.file && it.name == "build.gradle" }
        return gradleScripts.size() > 0
    }

    private void installSdkPackagesForGradleBuild() {
        def compileSdkVersions = []
        def buildToolsVersions = []

        forEachGradleScript { fileText ->
            compileSdkVersions += fileText.findAll("/compileSdkVersion\\s*(\\d*)/") { _, version -> "platforms;android-$version" }
            buildToolsVersions += fileText.findAll("/buildToolsVersion\\s*\"(.*)\"/") { _, version -> "build-tools;$version" }
        }

        installSdkPackages(compileSdkVersions + buildToolsVersions)
    }

    private void forEachGradleScript(Closure closure) {
        new File(".").eachFileRecurse(FileType.FILES) { file ->
            if (file.name == "build.gradle") {
                closure(file.getText())
            }
        }
    }

    private boolean isXamarinBuild() {
        def gradleScripts = new File(".").findAll { it.file && it.name.endsWith(".sln") }
        return gradleScripts.size() > 0
    }

    private void installSdkPackagesForXamarinBuild() {
        // TODO: Where do we pull compile SDK and build tools versions from for a Xamarin project?
    }

    void installSdkPackages(List<String> requestedPackages) {
        def packages = (requiredPackages + requestedPackages).collect { "'$it'" }.join(" ")

        def sdkHome = System.getenv("ANDROID_HOME")
        ["$sdkHome/tools/bin/sdkmanager", "--verbose", packages].execute()
        ["bash", "-c", "yes | $sdkHome/android/sdk/tools/bin/sdkmanager --licenses"].execute()
        ["bash", "-c", "$sdkHome/extras/intel/Hardware_Accelerated_Execution_Manager/silent_install.sh || true"].execute()
    }

}