plugins {
  kotlin("multiplatform")
  id("com.android.library")
  id("org.jetbrains.kotlin.plugin.compose")
  id("org.jetbrains.compose")
}

android {
  namespace = "com.twitpane.public_resource"

  compileSdk = rootProject.extra["compileSdkVersion"] as Int
  buildToolsVersion = rootProject.extra["buildToolsVersion"] as String

  defaultConfig {
    minSdk = rootProject.extra["minSdkVersion"] as Int
  }
}

kotlin {

  androidTarget {
    kotlin {
      jvmToolchain(11)
    }
  }

  listOf(
    iosX64(),
    iosArm64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "publicResourcesKit"
    }
  }

  sourceSets {
    commonMain {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.components.resources)
      }
    }
  }
}

compose.resources {
  publicResClass = true
  packageOfResClass = "twitpane.public_resources.resources"
  generateResClass = always
}

//--------------------------------------------------
// 文字列リソースの同期タスク
//--------------------------------------------------

// TODO shared/domain も含めて下記を共通化すること

// Compose Resource で定義した文字列ファイルを Android の res/values/strings.xml に同期するタスク
// ※Android のライブラリモジュールから R.string で参照できるようにするため
val syncStringsToAndroidRes by tasks.registering {
  val sourceDir = file("src/commonMain/composeResources")
  val targetDir = file("src/androidMain/res")

  outputs.dir(targetDir)

  doLast {
    println("🔄 Syncing Compose Resource strings.xml (all locales) to Android res/")

    sourceDir.walkTopDown().forEach { file ->
      // 対象: src/commonMain/composeResources/values[-xx]/strings.xml
      if (file.isFile && file.name == "strings.xml" && file.parentFile.name.startsWith("values")) {
        val relativePath = file.relativeTo(sourceDir) // e.g., values-ja/strings.xml
        val targetFile = targetDir.resolve(relativePath)

        println("📄 Copying ${file.path} → ${targetFile.path}")
        targetFile.parentFile.mkdirs()
        targetFile.writeText(file.readText())
      }
    }
  }
}
afterEvaluate {
  val tasksNeedingRes = listOf(
    "compileDebugKotlinAndroid",
    "compileReleaseKotlinAndroid",
    "extractDeepLinksDebug",
    "extractDeepLinksRelease",
    "generateComposeResClass",
    "generateDebugRFile",
    "generateDebugResources",
    "generateReleaseRFile",
    "generateReleaseResources",
    "extractDeepLinksForAarRelease",
  )

  tasksNeedingRes.forEach { name ->
    tasks.named(name).configure {
      dependsOn(syncStringsToAndroidRes)
    }
  }
}
