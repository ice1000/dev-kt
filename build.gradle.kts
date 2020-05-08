import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.*

var commitHash: String by extra
commitHash = Runtime
		.getRuntime()
		.exec("git rev-parse --short HEAD")
		.let { process ->
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
val kotlinEAP = "1.4.0-dev-8214"
isCI = !System.getenv("CI").isNullOrBlank()
isMac = SystemInfo.isMac
kotlinStable = "1.3.72"
kotlinVersion = if (isCI) kotlinEAP else kotlinStable

plugins {
	base
	idea
	java
	kotlin("jvm") version "1.3.72" apply false
}

idea {
	module {
		// https://github.com/gradle/kotlin-dsl/issues/537/
		excludeDirs = excludeDirs +
				file("pinpoint_piggy") +
				file("common/res/template") +
				file("swing/src/org/ice1000/devkt/ui/swing/forms") +
				file(".build-cache")
	}
	project {
		vcs = "Git"
	}
}

subprojects {
	val shortVersion = "v1.5-SNAPSHOT"
	val packageName = "org.ice1000.devkt"

	group = packageName
	version = if (isCI) "$shortVersion-$commitHash" else shortVersion

	repositories {
		mavenCentral()
		jcenter()
		maven("https://dl.bintray.com/kotlin/kotlin-dev")
		maven("https://dl.bintray.com/ice1000/ice1000")
	}

	apply {
		plugin("java")
		plugin("maven")
	}

	dependencies {
		implementation(kotlin("stdlib-jdk8", kotlinVersion))
		implementation(kotlin("reflect", kotlinVersion))
		implementation(kotlin("compiler-embeddable", kotlinVersion))
		implementation(kotlin("script-util", kotlinVersion))
		implementation(kotlin("script-runtime", kotlinVersion))
		implementation(kotlin("scripting-jsr223-embeddable", kotlinVersion))
		val textSeqVersion = "v0.4"
		implementation(group = "org.ice1000.textseq", name = "impl-gap", version = textSeqVersion)
		implementation(group = "org.ice1000.textseq", name = "common", version = textSeqVersion)
		// for the icon loader
		implementation(group = "com.bulenkov", name = "darcula", version = "2018.2")
		implementation(group = "com.bennyhuo.kotlin", name = "opd", version = "1.0-rc-2")

		testImplementation(group = "junit", name = "junit", version = "4.12")
		testImplementation(kotlin("test-junit", kotlinStable))
		testImplementation(kotlin("stdlib-jdk8", kotlinStable))
	}

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			jvmTarget = "1.8"
			freeCompilerArgs = listOf("-Xjvm-default=enable")
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

	val sourcesJar = task<Jar>("sourcesJar") {
		group = tasks["jar"].group.orEmpty()
		from(sourceSets["main"].allSource)
		archiveClassifier.set("sources")
	}

	artifacts {
		add("archives", sourcesJar)
	}
}


