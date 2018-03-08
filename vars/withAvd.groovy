def call(String hardwareProfile, String systemImage, Closure steps) {
    if (env.ANDROID_HOME == null) {
        failBuildWithError("ANDROID_HOME not defined, cannot launch AVD")
    }

    executeWithAvd(hardwareProfile, systemImage, steps)
}

private def executeWithAvd(String hardwareProfile, String systemImage, boolean headless = true, Closure steps = {}) {
    def avdName = env.BUILD_TAG.replaceAll("%2F", "-")
    def emulatorSerial = null
    try {
        installSystemImage(systemImage)
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

private def installSystemImage(String systemImage) {
    echo "Installing Android system image $systemImage"
    sh "$ANDROID_HOME/tools/bin/sdkmanager '$systemImage'"
    sh "yes | $ANDROID_HOME/tools/bin/sdkmanager --licenses"
}

private def createAvd(String avdName, String hardwareProfile, String systemImage) {
    echo "Creating AVD $avdName"
    sh "$ANDROID_HOME/tools/bin/avdmanager create avd -n $avdName -f -k '$systemImage' -d '$hardwareProfile' -c 100M"
}

private def launchAvd(String avdName, boolean headless) {
    def emulatorPort = firstAvailablePortInRange(5554, 5682)
    def emulatorSerial = "emulator-${emulatorPort}"
    def noWindowFlag = headless ? "-no-window" : ""

    echo "Launching AVD $avdName with serial $emulatorSerial"

    timeout(time: 5, unit: "MINUTES") {
        sh "$ANDROID_HOME/emulator/emulator -avd $avdName -port $emulatorPort -memory 2048 -partition-size 1024 $noWindowFlag -no-boot-anim -no-audio -no-snapshot &"
        sh "$ANDROID_HOME/platform-tools/adb -s $emulatorSerial wait-for-device"
        waitUntil {
            def bootCompleted = sh(script: "$ANDROID_HOME/platform-tools/adb -s ${emulatorSerial} shell getprop sys.boot_completed", returnStdout: true)
            return bootCompleted.trim() == "1"
        }
        sh "$ANDROID_HOME/platform-tools/adb -s $emulatorSerial shell input keyevent 82"
    }

    return emulatorSerial
}

private def firstAvailablePortInRange(start, end) {
    return sh(
            script: "(netstat -atn | awk '{printf \"%s\\n%s\\n\", \$4, \$4}' | grep -oE '[0-9]*\$'; seq ${start} ${end}) | sort -n | uniq -u | head -n 1",
            returnStdout: true
    ).trim()
}

private def deleteAvd(String avdName, String emulatorSerial) {
    sh "$ANDROID_HOME/platform-tools/adb -s $emulatorSerial emu kill || true"
    sh "$ANDROID_HOME/tools/bin/avdmanager delete avd -n $avdName || true"
}

private def failBuildWithError(String message) {
    echo message
    throw new IllegalStateException(message)
}
