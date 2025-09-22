plugins {
    kotlin("multiplatform") version "2.2.20"
    id("com.android.library")
}

group = "io.github"
version = "1.0"

repositories {
    google()
    mavenCentral()
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "p_sms"
        }
    }
    androidTarget()
    linuxX64("python") {
        binaries {
            sharedLib {
                baseName = "p_sms"
            }
        }
    }
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.ionspin.kotlin:bignum:0.3.10")
                implementation("com.soywiz.korlibs.krypto:krypto:4.0.10")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("com.google.android.material:material:1.13.0")
                implementation("commons-codec:commons-codec:1.19.0")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
        val iosMain by creating
        val iosArm64Main by getting
        val iosX64Main by getting
        val iosSimulatorArm64Main by getting
    }
}

android {
    namespace = "by.cyberpartisan.psms"
    compileSdkVersion(36)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk { version = release(21) }
    }

}

tasks.withType<Wrapper> {
    gradleVersion = "5.3.1"
    distributionType = Wrapper.DistributionType.ALL
}

