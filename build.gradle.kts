import org.gradle.api.internal.HasConvention
import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.*

var commitHash: String by extra
commitHash = Runtime
		.getRuntime()
		.exec("git rev-parse --short HEAD")
		.let<Process, String> { process ->
			process.waitFor()
			val output = process.inputStream.use {
				it.bufferedReader().use(BufferedReader::readText)
			}
			process.destroy()
			output.trim()
		}

var isCI: Boolean by extra
var isMac: Boolean by extra
var kotlinStable: String by extra
var kotlinVersion: String by extra
val kotlinEAP = "1.2.50-dev-880"
isCI = !System.getenv("CI").isNullOrBlank()
isMac = SystemInfo.isMac
kotlinStable = "1.2.40"
kotlinVersion = if (isCI) kotlinEAP else kotlinStable

plugins {
	base
	idea
	maven
	java
	kotlin("jvm") version "1.2.40" apply false
}

idea {
	module {
		// https://github.com/gradle/kotlin-dsl/issues/537/
		excludeDirs = excludeDirs +
				file("pinpoint_piggy") +
				file("common/res/template") +
				file(".build-cache")
	}
}

allprojects {
	val shortVersion = "v1.4-SNAPSHOT"
	val packageName = "org.ice1000.devkt"

	group = packageName
	version = if (isCI) "$shortVersion-$commitHash" else shortVersion

	repositories {
		mavenCentral()
		jcenter()
		maven("https://jitpack.io")
		maven("https://dl.bintray.com/kotlin/kotlin-dev")
	}

	apply {
		plugin("java")
	}

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			jvmTarget = "1.8"
			freeCompilerArgs = listOf("-Xenable-jvm-default")
			suppressWarnings = false
			verbose = isCI
		}
	}

	tasks.withType<JavaCompile> {
		sourceCompatibility = "1.8"
		targetCompatibility = "1.8"
		options.apply {
			isDeprecation = true
			isWarnings = true
			isDebug = !isCI
			compilerArgs.add("-Xlint:unchecked")
		}
	}
}

subprojects {
	apply {
		plugin("java")
		plugin("maven")
	}

	val sourcesJar = task<Jar>("sourcesJar") {
		group = tasks["jar"].group
		from(java.sourceSets["main"].allSource)
		classifier = "sources"
	}

	artifacts {
		add("archives", sourcesJar)
	}
}

