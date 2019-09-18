def call(String appIdentifier, String type) {
    withCredentials([
        string(credentialsId: "FASTLANE_MATCH_PASSWORD", variable: "MATCH_PASSWORD"),
        string(credentialsId: "FASTLANE_MATCH_GIT_URL", variable: "GIT_URL"),
        usernamePassword(credentialsId: 'APPLE_DEV_CENTRE_CREDENTIALS', passwordVariable: 'FASTLANE_PASSWORD', usernameVariable: 'FASTLANE_USER')
    ]) {
        sh "fastlane run match app_identifier:'${appIdentifier}' type:'${type}' git_url:'$GIT_URL' readonly:'true' verbose:'true'"
    }
}
