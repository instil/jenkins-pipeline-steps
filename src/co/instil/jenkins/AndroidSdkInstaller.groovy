package co.instil.jenkins

class AndroidSdkInstaller {

    private final requiredPackages = [
            "platform-tools",
            "tools",
            "emulator",
            "extras;android;m2repository",
            "extras;google;m2repository",
            "extras;intel;Hardware_Accelerated_Execution_Manager"
    ]

    void installSdkPackagesFromGradleScripts() {
        def compileSdkVersions = []
        def buildToolsVersions = []

        forEachGradleScript { fileText ->
            compileSdkVersions += fileText.findAll("/compileSdkVersion\\s*(\\d*)/") { _, version -> "platforms;android-$version" }
            buildToolsVersions += fileText.findAll("/buildToolsVersion\\s*\"(.*)\"/") { _, version -> "build-tools;$version" }
        }

        installSdkPackages(compileSdkVersions + buildToolsVersions)
    }

    private void forEachGradleScript(Closure closure) {
        new File(".").eachFileRecurse { file ->
            if (file.file && file.name == "build.gradle") {
                closure(file.getText())
            }
        }
    }

    void installSdkPackages(List<String> requestedPackages) {
        def sdkHome = System.getenv("ANDROID_HOME")
        if (sdkHome == null) {
            throw new IllegalArgumentException("ANDROID_HOME not defined, cannot install SDK packages")
        }

        def packages = (requiredPackages + requestedPackages).collect { "'$it'" }.join(" ")

        ["$sdkHome/tools/bin/sdkmanager", "--verbose", packages].execute()
        ["bash", "-c", "yes | $sdkHome/android/sdk/tools/bin/sdkmanager --licenses"].execute()
        ["bash", "-c", "$sdkHome/extras/intel/Hardware_Accelerated_Execution_Manager/silent_install.sh || true"].execute()
    }

}