def call(Map config) {

    String solution = config?.solution ?: findSolution()
    if (solution == null) {
    	failBuildWithError("No solution found. Please specify a solution explictly or ensure you are in the top level directory.")
    }

    echo "Building ${solution} …"

    String configuration = config?.configuration ?: "Release"
    String platform = config?.platform ?: "iPhone"
    boolean buildIpa = config?.buildIpa ?: true
    boolean archive = config?.archive ?: true

    build(solution, configuration, platform, buildIpa, archive)
}

private def build(String solution, String configuration, String platform, boolean buildIpa, boolean archive) {
	withCredentials([string(credentialsId: "FASTLANE_KEYCHAIN_PASSWORD", variable: "KEYCHAIN_PASSWORD")]) {
        def keychain = "$HOME/Library/Keychains/fastlane.keychain-db"
        sh "security unlock-keychain -p $keychainPassword $keychain"
        sh "msbuild /p:Configuration=$configuration /p:Platform=$platform /p:BuildIpa=$buildIpa /p:ArchiveOnBuild=$archive /t:Clean /t:Build $solution /p:CodesignKeychain=$keychain"
        sh "security lock-keychain $keychain"
    }
}

private def findSolution() {
	return sh(script:"find . -type f -name '*.sln'")
		.split()
		.toList()
		.first()
}

private def failBuildWithError(String message) {
    echo message
    throw new IllegalStateException(message)
}
