package co.instil.jenkins

import static java.lang.System.currentTimeMillis

class AvdLauncher {

    private final String sdkHome

    AvdLauncher(String sdkHome) {
        this.sdkHome = sdkHome
    }

    void executeWithAvd(String name, String hardwareProfile, String systemImage, Closure steps) {
        def emulatorSerial = null
        try {
            createAvd(name, hardwareProfile, systemImage)
            emulatorSerial = launchAvd(name)
            steps(emulatorSerial)
        } catch(any) {
            throw any
        } finally {
            deleteAvd(name, emulatorSerial)
        }
    }

    private void createAvd(String name, String hardwareProfile, String systemImage) {
        installSystemImage(systemImage)
        "$sdkHome/tools/bin/avdmanager create avd -n $name -f -k $systemImage -d $hardwareProfile".execute()
    }

    private void installSystemImage(String systemImage) {
        new AndroidSdkInstaller(sdkHome).installSdkPackages([systemImage])
    }

    private String launchAvd(String name) {
        def emulatorPort = findFirstFreeEmulatorPort()
        def emulatorSerial = "emulator-${emulatorPort}"

        "$sdkHome/emulator/emulator -avd $name -port $emulatorPort -no-window -no-boot-anim -no-audio -writable-system &".execute()
        "$sdkHome/platform-tools/adb -s $emulatorSerial wait-for-device".execute()
        waitUntilBootCompleted()

        return emulatorSerial
    }

    private int findFirstFreeEmulatorPort() {
        return (5554..5682).step(2).find { isPortFree(it) }
    }

    private boolean isPortFree(int port) {
        try {
            def socket = new ServerSocket(port)
            socket.setReuseAddress(true)
            socket.close();
            return true
        } catch (any) {
            return false
        }
    }

    private void waitUntilBootCompleted() {
        def startTime = currentTimeMillis()
        while (!bootCompleted()) {
            Thread.sleep(1000)
            if ((currentTimeMillis() - startTime) > 60000) {
                break
            }
        }
    }

    private boolean bootCompleted() {
        def bootCompleted = "$sdkHome/platform-tools/adb -s $emulatorSerial shell getprop sys.boot_completed"
        return bootCompleted.execute().text == "1"
    }

    private void deleteAvd(String name, String emulatorSerial) {
        killEmulator(emulatorSerial)
        "$sdkHome/tools/bin/avdmanager delete avd -n $name".execute()
    }

    private void killEmulator(String emulatorSerial) {
        if (emulatorSerial != null) {
            "$sdkHome/platform-tools/adb -s $emulatorSerial emu kill".execute()
        }
    }

}
