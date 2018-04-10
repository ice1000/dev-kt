import de.undercouch.gradle.tasks.download.Download
import groovy.lang.Closure
import org.gradle.api.internal.HasConvention
import org.gradle.internal.deployment.RunApplication
import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.awt.HeadlessException
import java.io.*
import java.nio.file.*
import java.util.concurrent.*
import java.util.stream.Collectors
import javax.swing.*

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

val pluginShortVersion = "v1.0"
val packageName = "org.ice1000.devkt"
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
	idea
	java
	application
	id("org.jetbrains.intellij") version "0.3.1"
	id("de.undercouch.download") version "3.4.2"
	kotlin("jvm") version "1.2.31"
}

application {
	if (SystemInfo.isMac)
		applicationDefaultJvmArgs = arrayListOf("-Xdock:name=Dev-Kt")
	mainClassName = "org.ice1000.devkt.Main"
}

intellij {
	instrumentCode = true
	if (ext.has("ideaC_path")) {
		localPath = ext["ideaC_path"].toString()
	} else {
		if (isCI) return@intellij
		try {
			println("Please specify your IntelliJ IDEA installation path:")
			val line = readLine()?.trim()
			if (null != line && Files.exists(Paths.get(line))) {
				localPath = line
				file("gradle.properties").writeText("ideaC_path=$line")
			} else version = "2018.1"
		} catch (e: HeadlessException) {
			e.printStackTrace()
			version = "2018.1"
		}
	}
}

apply {
	plugin("kotlin")
	plugin("de.undercouch.download")
}

val disabledTasks = listOf("assembleDist",
		"distZip",
		"distTar",
		"installDist",
		"runIde",
		"verifyPlugin",
		"buildPlugin",
		"prepareSandbox",
		"prepareTestingSandbox",
		"patchPluginXml",
		"publishPlugin"
)

tasks.removeIf {
	if (it.name in disabledTasks) {
		it.enabled = false
		true
	} else false
}

idea {
	module {
		// https://github.com/gradle/kotlin-dsl/issues/537/
		excludeDirs = excludeDirs +
				file("pinpoint_piggy") +
				file("build-cache") +
				Paths.get("res", "template").toFile()
	}
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
		attributes(mapOf("Main-Class" to application.mainClassName,
				"SplashScreen-Image" to "icon/kotlin@288x288.png"))
	}
}

val downloadFiraCode = task<Download>("downloadFiraCode") {
	src("https://raw.githubusercontent.com/tonsky/FiraCode/master/distr/ttf/FiraCode-Regular.ttf")
	dest(Paths.get("res", "font").toFile())
	overwrite(false)
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
		resources.setSrcDirs(listOf("testRes"))
	}
}

repositories {
	mavenCentral()
	jcenter()
	maven("https://jitpack.io")
}

dependencies {
	compile(kotlin("stdlib-jdk8", kotlinVersion))
	compile(kotlin("compiler-embeddable", kotlinVersion))
	compile(group = "com.github.cqjjjzr", name = "Gensokyo", version = "1.1")
	compile(group = "com.github.ice1k", name = "darcula", version = "2018.1")
	compile(group = "com.intellij", name = "forms_rt", version = "7.0.3")
	compile(files(Paths.get("lib", "filedrop.jar")))
	configurations.compileOnly.exclude(group = "com.jetbrains", module = "ideaLocal")
	compileOnly(files(Paths.get("lib", "AppleJavaExtensions-1.6.jar")))
	testCompile("junit", "junit", "4.12")
	testCompile(kotlin("test-junit", kotlinVersion))
	testCompile(kotlin("stdlib-jdk8", kotlinVersion))
	configurations.runtime.extendsFrom(configurations.testCompileOnly)
}
