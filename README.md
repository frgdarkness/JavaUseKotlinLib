﻿# JavaUseKotlinLib

build.gradle (project)

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url "https://jitpack.io" }
        maven { url 'https://dl.bintray.com/kandroid/maven' }
    }
    dependencies {
        def nav_version = "2.5.3"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
        classpath 'com.squareup.sqldelight:gradle-plugin:1.5.4'
        classpath "org.jetbrains.kotlin:kotlin-serialization:1.8.0"
        ext.kotlin_version = '1.4.10'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0"

        classpath "org.jetbrains.kotlin:kotlin-serialization:1.8.20"
        classpath 'com.google.gms:google-services:4.3.15'
    }
}
plugins {
    id 'com.android.application' version '8.0.2' apply false
    id 'com.android.library' version '8.0.2' apply false
}

------------------------------------------------------------------------------------------------

build.gradle (app)
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'org.jetbrains.kotlin.android'
    id 'kotlin-parcelize'
    id 'kotlinx-serialization'
}

android {
  ...
  compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}

dependencies {
  ...
  implementation project(path: ':ZeroImageEditor')
    implementation 'androidx.core:core-ktx:1.3.2'
    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.8.0"
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.0"
  ...
}

------------------------------------------------------------------------------------------------

setting.gradle

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven { url "https://jitpack.io" }
        maven { url 'https://dl.bintray.com/kandroid/maven' }
    }
}
rootProject.name = "JavaAppTest"
include ':app'
include ':KotlinToastLibrary'
include ':ZeroImageEditor'


