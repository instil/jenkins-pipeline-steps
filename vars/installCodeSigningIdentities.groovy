def call(String appIdentifier, String type, String matchPasswordCredentialsId = "FASTLANE_MATCH_PASSWORD", String gitUrlCredentialsId = "FASTLANE_MATCH_GIT_URL", boolean generateAppleCerts=false) {
    withCredentials([
        string(credentialsId: "FASTLANE_KEYCHAIN_PASSWORD", variable: "KEYCHAIN_PASSWORD"),
        string(credentialsId: matchPasswordCredentialsId, variable: "MATCH_PASSWORD"),
        string(credentialsId: gitUrlCredentialsId, variable: "GIT_URL"),
        usernamePassword(credentialsId: 'APPLE_DEV_CENTRE_CREDENTIALS', passwordVariable: 'FASTLANE_PASSWORD', usernameVariable: 'FASTLANE_USER')
    ]) {
        sh "fastlane run match app_identifier:'${appIdentifier}' type:'${type}' keychain_name:'fastlane.keychain' keychain_password:'$KEYCHAIN_PASSWORD' git_url:'$GIT_URL' readonly:'true' verbose:'true' generate_apple_certs:'${generateAppleCerts}'"
    }
}
