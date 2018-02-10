A collection of build steps for use with Jenkins pipelines packaged as a shared library. 

# Usage

Fetch and load the library following the guidelines from the [Jenkins documentation](https://jenkins.io/doc/book/pipeline/shared-libraries/#using-libraries).

# Available Steps

## Android

### installAndroidSdkPackages

Build step which automates installation of Android SDK components either through explicit declaration or by
scanning the workspace for Gradle and Xamarin projects to extract the required SDK components from project files.

The following components will be installed and configured automatically:

* platform-tools
* tools
* emulator
* extras;android;m2repository
* extras;google;m2repository
* extras;intel;Hardware_Accelerated_Execution_Manager

To scan the job workspace for Gradle and Xamarin projects, simply call the step as follows:

```
node {
    installAndroidSdkPackages()
}
```

To install packages explicitly, just pass a list of the packages to the `installAndroidSdkPackages`
like below. Note that package names should match the Android `sdkmanager` format.

```
node {
    installAndroidSdkPackages([
        "build-tools;27",
        "platform-27"
    ])
}
```

### withAvd

Build step for launching Android AVDs - an AVD will be created with a unique name for the build currently
being executed, launched then destroyed when the nested steps have completed. The step takes three arguments:

* Hardware profile - the ID of an Android SDK hardware profile. Only the default SDK provided hardware profiles are supported which can be found by running `avdmanager list`.
* System image - the system image to be used for the AVD, this should be in the sdkmanager format e.g. system-images;android-27;default;x86. The system image will be automatically installed if not already available.
* A closure containing the build steps to execute while the AVD is running.

```
node {
    withAvd("Nexus 5X", "system-images;android-27;default;x86") {
        sh "./gradlew connectedAndroidTest"
    }
}
```

Note that this build step will automatically set the `ANDROID_SERIAL` environment variable so that the Gradle `connectedAndroidTest`
tasks knows which emulator to target if multiple AVDs have been launched or concurrent builds are in progress for a build node.