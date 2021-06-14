def call(Map config, Closure steps) {
    checkSdkInstallation()

    String hardwareProfile = config.hardwareProfile
    String systemImage = config.systemImage
    boolean headless = config.containsKey('headless') ? config.headless : true

    if (hardwareProfile == null || systemImage == null) {
        failBuildWithError("hardwareProfile and systemImage must be provided for launching AVD")
    }

    executeWithAvd(hardwareProfile, systemImage, headless, steps)
}

private def executeWithAvd(String hardwareProfile, String systemImage, boolean headless, Closure steps) {
    def avdName = env.BUILD_TAG.replaceAll(/%20|%2F|\s/, "-")
    def emulatorSerial = null
    try {
        installAndroidSdkPackages([systemImage])
        createAvd(avdName, hardwareProfile, systemImage)
        emulatorSerial = launchAvd(avdName, headless)

        // Set ANDROID_SERIAL env var so the Gradle connectedAndroidTest task knows which
        // emulator instance to target if we have multiple AVDs or concurrent jobs.
        withEnv(["ANDROID_SERIAL=$emulatorSerial"]) {
            steps()
        }
    } catch(any) {
        throw any
    } finally {
        deleteAvd(avdName, emulatorSerial)
    }
}

private def createAvd(String avdName, String hardwareProfile, String systemImage) {
    echo "Creating AVD $avdName"
    sh "$ANDROID_CMDLINE_TOOLS_ROOT/bin/avdmanager create avd -n $avdName -f -k '$systemImage' -d '$hardwareProfile' -c 100M"
}

private def launchAvd(String avdName, boolean headless) {
    def emulatorPort = firstAvailablePortInRange(5554, 5682)
    def emulatorSerial = "emulator-${emulatorPort}"
    def noWindowFlag = headless ? "-no-window" : ""

    echo "Launching AVD $avdName with serial $emulatorSerial"

    timeout(time: 5, unit: "MINUTES") {
        sh "$ANDROID_SDK_ROOT/emulator/emulator -avd $avdName -port $emulatorPort -memory 2048 -partition-size 1024 $noWindowFlag -no-boot-anim -no-audio -no-snapshot &"
        sh "$ANDROID_SDK_ROOT/platform-tools/adb -s $emulatorSerial wait-for-device"
        waitUntil {
            def bootCompleted = sh(script: "$ANDROID_SDK_ROOT/platform-tools/adb -s ${emulatorSerial} shell getprop sys.boot_completed", returnStdout: true)
            return bootCompleted.trim() == "1"
        }
        sh "$ANDROID_SDK_ROOT/platform-tools/adb -s $emulatorSerial shell settings put global window_animation_scale 0"
        sh "$ANDROID_SDK_ROOT/platform-tools/adb -s $emulatorSerial shell settings put global transition_animation_scale 0"
        sh "$ANDROID_SDK_ROOT/platform-tools/adb -s $emulatorSerial shell settings put global animator_duration_scale 0"
        sh "$ANDROID_SDK_ROOT/platform-tools/adb -s $emulatorSerial shell input keyevent 82"
    }

    return emulatorSerial
}

private def firstAvailablePortInRange(start, end) {
    def netstatAvailable = sh(script: "command -v netstat", returnStdout: true)
    def netstatCommand = netstatAvailable ? "netstat" : "ss"
    return sh(
      script: "(${netstatCommand} -atn | awk '{printf \"%s\\n%s\\n\", \$4, \$4}' | grep -oE '[0-9]*\$'; seq ${start} ${end}) | sort -n | uniq -u | head -n 1",
      returnStdout: true
    ).trim()
}

private def deleteAvd(String avdName, String emulatorSerial) {
    sh "$ANDROID_SDK_ROOT/platform-tools/adb -s $emulatorSerial emu kill || true"
    sh "$ANDROID_CMDLINE_TOOLS_ROOT/bin/avdmanager delete avd -n $avdName || true"
}

private def failBuildWithError(String message) {
    echo message
    throw new IllegalStateException(message)
}
