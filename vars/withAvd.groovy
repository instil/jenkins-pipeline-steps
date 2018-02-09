import co.instil.jenkins.AndroidSdkInstaller
import co.instil.jenkins.AvdLauncher

def call(String hardwareProfile, String systemImage, Closure steps) {
    if (env.ANDROID_HOME == null) {
        failBuildWithError("ANDROID_HOME not defined, cannot launch AVD")
    }

    new AvdLauncher().executeWithAvd(env.BUILD_TAG, hardwareProfile, systemImage) { emulatorSerial ->
        // Set ANDROID_SERIAL env var so connectedAndroidTest knows which emulator
        // instance to target if we have multiple AVDs or concurrent jobs.
        env.ANDROID_SERIAL = emulatorSerial
        steps()
    }
}

private def failBuildWithError(String message) {
    echo message
    throw new IllegalArgumentException(message)
}