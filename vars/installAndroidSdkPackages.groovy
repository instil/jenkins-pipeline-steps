import co.instil.jenkins.AndroidSdkInstaller

def call(List<String> packages = null) {
    def sdkInstaller = new AndroidSdkInstaller()

    if (packages != null) {
        sdkInstaller.installSdkPackages(packages)
    } else {
        sdkInstaller.installSdkPackagesFromGradleScripts()
    }
}