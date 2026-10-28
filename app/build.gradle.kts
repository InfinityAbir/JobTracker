plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
}

android {
  namespace = "com.example"
  compileSdk { version = release(37) }

  defaultConfig {
    applicationId = "com.aistudio.jobtracker.app"
    minSdk = 24
    targetSdk = 36
    versionCode = 3
    versionName = "1.0.2"

    // Public URL of the deployed ai-proxy Worker (not a secret). Empty = shared AI disabled, own-key only.
    val aiProxyUrl = (project.findProperty("aiProxyUrl") as String?)?.trim().orEmpty()
    buildConfigField("String", "AI_PROXY_URL", "\"$aiProxyUrl\"")

    // Where the in-app "Send Tester Feedback" button sends mail. Empty = the user's mail app opens without a recipient.
    val feedbackEmail = (project.findProperty("feedbackEmail") as String?)?.trim().orEmpty()
    buildConfigField("String", "FEEDBACK_EMAIL", "\"$feedbackEmail\"")

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // Local debug keystore. `debug.keystore` is git-ignored and generated on demand, so the
  // project falls back to the default Android debug keystore when it is absent.
  val debugKeystore = file("${rootDir}/debug.keystore")

  // Upload key used for release builds. Override the path and passwords via environment
  // variables (KEYSTORE_PATH, STORE_PASSWORD, KEY_PASSWORD) for CI or custom keys.
  // Defaults point to the project's production keystore at E:/Keys/jobtracker-upload.jks.
  val releaseKeystorePath = System.getenv("KEYSTORE_PATH") ?: "E:/Keys/jobtracker-upload.jks"
  val releaseKeystore = file(releaseKeystorePath)
  val releaseStorePassword = System.getenv("STORE_PASSWORD") ?: ""
  val releaseKeyPassword = System.getenv("KEY_PASSWORD") ?: ""
  val hasReleaseKeystore = releaseKeystore.exists() && releaseStorePassword.isNotBlank() && releaseKeyPassword.isNotBlank()

  signingConfigs {
    create("release") {
      storeFile = releaseKeystore.takeIf { hasReleaseKeystore }
      storePassword = releaseStorePassword
      keyAlias = "upload"
      keyPassword = releaseKeyPassword
    }
    create("debugConfig") {
      storeFile = debugKeystore.takeIf { it.exists() }
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = if (hasReleaseKeystore) signingConfigs.getByName("release") else signingConfigs.getByName("debug")
    }
    debug {
      signingConfig = if (debugKeystore.exists()) signingConfigs.getByName("debugConfig") else signingConfigs.getByName("debug")
      // Optional: -PdebugAppIdSuffix=.vt installs the debug build next to the Play build instead of replacing it.
      (project.findProperty("debugAppIdSuffix") as String?)?.takeIf { it.isNotBlank() }?.let { applicationIdSuffix = it }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Never ship a Play upload build signed with the debug key: Play rejects it, and a debug-signed
// APK must not reach users. Pass -PallowDebugSignedRelease=true for local R8/smoke builds only.
gradle.taskGraph.whenReady {
  val releaseKeyMissing = !(file(System.getenv("KEYSTORE_PATH") ?: "E:/Keys/jobtracker-upload.jks").exists() &&
    !System.getenv("STORE_PASSWORD").isNullOrBlank() && !System.getenv("KEY_PASSWORD").isNullOrBlank())
  val buildsRelease = allTasks.any {
    it.project == project && Regex("^(assemble|bundle|package)Release$").matches(it.name)
  }
  if (buildsRelease && releaseKeyMissing && project.findProperty("allowDebugSignedRelease") != "true") {
    throw GradleException(
      "Release keystore not found or STORE_PASSWORD/KEY_PASSWORD not set. " +
        "Set KEYSTORE_PATH, STORE_PASSWORD and KEY_PASSWORD, or pass -PallowDebugSignedRelease=true for a local-only build."
    )
  }
}

// Exported Room schemas (app/schemas) are the baseline for writing and testing migrations.
ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.okhttp)
  testImplementation(libs.turbine)
  testImplementation(libs.mockito.kotlin)
  testImplementation(libs.mockito.core)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
}
