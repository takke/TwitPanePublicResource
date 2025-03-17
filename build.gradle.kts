plugins {
  id("com.android.library")
}

android {
  namespace = "com.twitpane.public_resource"

  compileSdk = rootProject.extra["compileSdkVersion"] as Int
  buildToolsVersion = rootProject.extra["buildToolsVersion"] as String

  defaultConfig {
    minSdk = rootProject.extra["minSdkVersion"] as Int
    targetSdkVersion(rootProject.extra["targetSdkVersion"] as Int)
  }
} 