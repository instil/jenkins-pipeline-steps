import co.instil.jenkins.AndroidSdkInstaller

def call(List<String> packages = null) {
    if (env.ANDROID_HOME == null) {
        failBuildWithError("ANDROID_HOME not defined, cannot install SDK packages")
    }

    def sdkInstaller = new AndroidSdkInstaller()
    if (packages != null) {
        sdkInstaller.installSdkPackages(packages)
    } else {
        sdkInstaller.scanRepositoryForBuildScripts()
    }
}

private def failBuildWithError(String message) {
    echo message
    throw new IllegalArgumentException(message)
}