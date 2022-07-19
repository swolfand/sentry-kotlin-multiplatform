import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    kotlin("plugin.serialization") version "1.7.10"
    id("com.android.library")
    `maven-publish`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

group = "io.sentry.kotlin.multiplatform"
version = "0.0.1"

android {
    compileSdk = 30
    defaultConfig {
        minSdk = 16
        targetSdk = 30
        version = "0.0.1"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    // linking the manifest file manually due to having it in the "androidMain" source set
    sourceSets.getByName("main").manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

kotlin {
    android {
        publishAllLibraryVariants()
    }

    jvm()
    ios()
    iosSimulatorArm64()

    /*
    watchos()
    tvos()
    macosX64()
     */

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("io.ktor:ktor-client-serialization:2.0.3")
                implementation("io.ktor:ktor-client-core:2.0.3")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.sentry:sentry:6.1.4")
            }
        }
        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-junit")
                implementation("io.ktor:ktor-client-okhttp:2.0.3")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("io.sentry:sentry-android:6.1.4")
            }
        }
        val androidTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-junit")
                implementation("junit:junit:4.13.1")
                implementation("androidx.test:core:1.4.0")
                implementation("androidx.test.ext:junit:1.1.3")
                implementation("org.robolectric:robolectric:4.5.1")
                implementation("io.ktor:ktor-client-okhttp:2.0.3")
            }
        }

        val appleMain by creating { dependsOn(commonMain) }
        val appleTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation("io.ktor:ktor-client-darwin:2.0.3")
            }
        }

        val iosMain by getting { dependsOn(appleMain) }
        val iosTest by getting {
            dependsOn(appleTest)
        }

        val iosSimulatorArm64Main by getting { dependsOn(appleMain) }
        val iosSimulatorArm64Test by getting { dependsOn(appleTest) }

        cocoapods {
            summary = "Official Sentry SDK for iOS / tvOS / macOS / watchOS"
            homepage = "https://github.com/getsentry/sentry-cocoa"

            pod("Sentry", "~> 7.19.0")

            ios.deploymentTarget = "9.0"
            // osx.deploymentTarget = "10.10"
            // tvos.deploymentTarget = "9.0"
            // watchos.deploymentTarget = "2.0"
        }

        /*
        val appleMain by creating { dependsOn(commonMain) }
        val iosMain by getting { dependsOn(appleMain) }
        val tvosMain by getting { dependsOn(appleMain) }
        val watchosMain by getting { dependsOn(appleMain) }
        val macosX64Main by getting { dependsOn(appleMain) }
*/
    }

    // workaround for https://youtrack.jetbrains.com/issue/KT-41709 due to having "Meta" in the class name
    // if we need to use this class, we'd need to find a better way to work it out
    targets.withType<KotlinNativeTarget>().all {
        compilations["main"].cinterops["Sentry"].extraOpts(
            "-compiler-option",
            "-DSentryMechanismMeta=SentryMechanismMetaUnavailable"
        )
    }
}
