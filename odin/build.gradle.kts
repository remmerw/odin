import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.remmerw"
version = "0.2.4"


kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }


    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()


    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.androidx.sqlite.bundled)
                implementation(libs.androidx.room.runtime)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.io.core)
                implementation(libs.uri.kmp)
                implementation(libs.androidx.datastore.preferences.core)
                implementation(libs.androidx.datastore.preferences)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
                implementation(libs.qrose)
                implementation(libs.lifecycle)

                implementation("io.github.remmerw:asen:0.2.8") // todo
                implementation("io.github.remmerw:idun:0.2.8") // todo

                implementation("io.github.vinceglb:filekit-core:0.10.0-beta04") // todo
                implementation("io.github.vinceglb:filekit-dialogs:0.10.0-beta04") // todo
                implementation("io.github.vinceglb:filekit-dialogs-compose:0.10.0-beta04") // todo

                implementation(libs.connectivity.core)
                implementation(libs.connectivity.compose)

            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidUnitTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        iosMain {
            dependencies {
                implementation(libs.connectivity.device)
                implementation(libs.connectivity.compose.device)
                implementation(libs.connectivity.apple)
            }
        }

        jvmMain {
            dependencies {
                implementation(compose.desktop.common)
                implementation(libs.connectivity.http)
                implementation(libs.connectivity.compose.http)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.androidx.work.runtime)
                implementation(libs.connectivity.device)
                implementation(libs.connectivity.compose.device)
                implementation(libs.connectivity.android)
            }
        }
    }
}


android {
    namespace = "io.github.remmerw.odin"
    compileSdk = 36
    defaultConfig {
        minSdk = 27
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}



dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
}



mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "odin", version.toString())

    pom {
        name = "odin"
        description = "API library for Odin application"
        inceptionYear = "2025"
        url = "https://github.com/remmerw/odin/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "remmerw"
                name = "Remmer Wilts"
                url = "https://github.com/remmerw/"
            }
        }
        scm {
            url = "https://github.com/remmerw/odin/"
            connection = "scm:git:git://github.com/remmerw/odin.git"
            developerConnection = "scm:git:ssh://git@github.com/remmerw/odin.git"
        }
    }
}
