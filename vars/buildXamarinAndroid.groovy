def call(Map config) {

    String project = config?.project
    if (project == null) {
    	failBuildWithError("Please specify a project to build.")
    }

    String keystorePath = config?.keystorePath
    if (keystorePath == null) {
        failBuildWithError("Please specify a keystore.")
    }

    String keystoreCredentialsId = config?.keystoreCredentialsId
    if (keystoreCredentialsId == null) {
        failBuildWithError("Please specify a keystore credentials ID for extracting the keystore password from the Jenkins keychain.")
    }

    String keystoreAlias = config?.keystoreAlias
    if (keystoreAlias == null) {
        failBuildWithError("Please specify a keystore alias for extracting the key from the keystore.")
    }

    echo "Building $project …"

    String configuration = config?.configuration ?: "Release"
    build(project, configuration, keystorePath, keystoreCredentialsId, keystoreAlias)
}

private def build(String project, String configuration, String keystorePath, String keystoreCredentialsId, String keystoreAlias) {
	withCredentials([string(credentialsId: keystoreCredentialsId, variable: "KEYSTORE_PASSWORD")]) {
        sh "msbuild /p:Configuration=$configuration /t:Clean /t:SignAndroidPackage /p:AndroidKeyStore=true /p:AndroidSigningKeyAlias=$keystoreAlias /p:AndroidSigningKeyPass=$KEYSTORE_PASSWORD /p:AndroidSigningKeyStore=$keystorePath /p:AndroidSigningStorePass=$KEYSTORE_PASSWORD $project"
    }
}

private def failBuildWithError(String message) {
    echo message
    throw new IllegalStateException(message)
}
