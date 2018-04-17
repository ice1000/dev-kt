import org.gradle.api.internal.HasConvention
import org.gradle.internal.deployment.RunApplication
import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.*
import java.nio.file.*
import java.util.concurrent.*
import java.util.stream.Collectors

val commitHash by lazy {
	val process: Process = Runtime.getRuntime().exec("git rev-parse --short HEAD")
	process.waitFor()
	val output = process.inputStream.use {
		it.bufferedReader().use(BufferedReader::readText)
	}
	process.destroy()
	output.trim()
}

val isCI = !System.getenv("CI").isNullOrBlank()

val pluginShortVersion = "v1.0-SNAPSHOT"
val packageName = "devkt"
val kotlinVersion: String by extra
val pluginCalculatedVersion = if (isCI) "$pluginShortVersion-$commitHash" else pluginShortVersion

group = packageName
version = pluginCalculatedVersion

buildscript {
	var kotlinVersion: String by extra
	kotlinVersion = "1.2.31"

	repositories {
		mavenCentral()
	}

	dependencies {
		classpath(kotlin("gradle-plugin", kotlinVersion))
	}
}

plugins {
	java
	application
	kotlin("jvm") version "1.2.31"
}

application {
	if (SystemInfo.isMac)
		applicationDefaultJvmArgs = arrayListOf("-Xdock:name=Dev-Kt")
	mainClassName = "devkt.DevKtCompiledKt"
}

apply {
	plugin("kotlin")
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		jvmTarget = "1.8"
	}
}

tasks.withType<Jar> {
	manifest {
		attributes(mapOf("Main-Class" to application.mainClassName))
	}
}

val fatJar = task<Jar>("fatJar") {
	classifier = "all"
	description = "Assembles a jar archive containing the main classes and all the dependencies."
	group = "build"
	from(Callable {
		configurations.compile.map {
			@Suppress("IMPLICIT_CAST_TO_ANY")
			if (it.isDirectory) it else zipTree(it)
		}
	})
	with(tasks["jar"] as Jar)
}

val SourceSet.kotlin
	get() = (this as HasConvention).convention.getPlugin(KotlinSourceSet::class.java).kotlin

java.sourceSets {
	"main" {
		java.setSrcDirs(listOf("src"))
		kotlin.setSrcDirs(listOf("src"))
		resources.setSrcDirs(listOf("res"))
	}

	"test" {
		java.setSrcDirs(listOf("test"))
		kotlin.setSrcDirs(listOf("test"))
	}
}

repositories {
	mavenCentral()
	jcenter()
	maven("https://jitpack.io")
}

dependencies {
	compile(kotlin("stdlib-jdk8", kotlinVersion))
	testCompile("junit", "junit", "4.12")
	testCompile(kotlin("test-junit", kotlinVersion))
	testCompile(kotlin("stdlib-jdk8", kotlinVersion))
}
