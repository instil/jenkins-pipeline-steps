def call() {
    if (env.ANDROID_SDK_ROOT == null && env.ANDROID_HOME == null) {
        failBuildWithError("ANDROID_SDK_ROOT not defined, cannot launch AVD")
    } else if (env.ANDROID_SDK_ROOT == null && env.ANDROID_HOME != null) {
        // Compatibility with legacy
        env.ANDROID_SDK_ROOT = env.ANDROID_HOME
    }

    if (env.ANDROID_CMDLINE_TOOLS_ROOT == null || !env.ANDROID_CMDLINE_TOOLS_ROOT.startsWith(env.ANDROID_SDK_ROOT)) {
        echo "Checking command-line tools installation"
        String cmdlineToolsRoot = "$ANDROID_SDK_ROOT/cmdline-tools/latest/"

        boolean test = fileExists file: "$cmdlineToolsRoot/bin/sdkmanager"

        if (!test) {
            // Compatibilty with legacy
            cmdlineToolsRoot = "$ANDROID_SDK_ROOT/tools/"
            test = fileExists file: "$cmdlineToolsRoot/bin/sdkmanager"

            if(!test) {
                failBuildWithError("No Android SDK found at $ANDROID_SDK_ROOT/cmdline-tools/latest/")
            }
        }

        env.ANDROID_CMDLINE_TOOLS_ROOT = cmdlineToolsRoot
    }
}

private def failBuildWithError(String message) {
    echo message
    throw new IllegalStateException(message)
}
