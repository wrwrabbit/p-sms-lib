plugins {
    kotlin("multiplatform") version "1.5.10"
    id("com.android.library")
}

group = "io.github"
version = "1.0"

repositories {
    google()
    jcenter()
    mavenCentral()
}

kotlin {
    iosArm64("ios") {
        binaries {
            framework {
                baseName = "p_sms"
            }
        }
    }
    android()
    linuxX64("python") {
        binaries {
            sharedLib {
                baseName = "p_sms"
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.ionspin.kotlin:bignum:0.3.1")
                implementation("com.soywiz.korlibs.krypto:krypto:2.1.1")
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
                implementation("com.google.android.material:material:1.2.1")
                implementation( "commons-codec:commons-codec:1.15")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13")
            }
        }
        val iosMain by getting {
            dependsOn(commonMain)
        }
    }
}

android {
    compileSdkVersion(29)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
    }

}

tasks.withType<Wrapper> {
    gradleVersion = "5.3.1"
    distributionType = Wrapper.DistributionType.ALL
}

