plugins {
    id("com.android.application") version "8.7.2" apply false
    // alias(libs.plugins.androidApplication) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.2") // 이 부분 수정
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
