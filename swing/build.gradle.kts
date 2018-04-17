import de.undercouch.gradle.tasks.download.Download
import org.gradle.internal.deployment.RunApplication
import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.awt.HeadlessException
import java.nio.file.*
import java.util.concurrent.*

val commitHash: String by rootProject.extra
val isCI: Boolean by rootProject.extra

plugins {
	idea
	java
	application
	id("org.jetbrains.intellij") version "0.3.1"
	id("de.undercouch.download") version "3.4.2"
	kotlin("jvm")
}

application {
	if (SystemInfo.isMac)
		applicationDefaultJvmArgs = listOf("-Xdock:name=Dev-Kt")
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
			if (null != line && file(line).exists()) {
				localPath = line
				file("gradle.properties").writeText("ideaC_path=$line")
			} else version = "2018.1"
		} catch (e: HeadlessException) {
			e.printStackTrace()
			version = "2018.1"
		}
	}
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

task<Jar>("fatJar") {
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

idea {
	module {
		// https://github.com/gradle/kotlin-dsl/issues/537/
		excludeDirs = excludeDirs +
				file("pinpoint_piggy") +
				file("build-cache") +
				file(".build-cache")
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

java.sourceSets {
	"main" {
		resources.setSrcDirs(listOf("res"))
		java.setSrcDirs(listOf("src"))
		withConvention(KotlinSourceSet::class) {
			kotlin.setSrcDirs(listOf("src"))
		}
	}

	"test" {
		resources.setSrcDirs(listOf("testRes"))
		java.setSrcDirs(listOf("test"))
		withConvention(KotlinSourceSet::class) {
			kotlin.setSrcDirs(listOf("test"))
		}
	}
}

dependencies {
	val kotlinStable: String by rootProject.extra
	compile(project(":common"))
	compile(group = "com.github.cqjjjzr", name = "Gensokyo", version = "1.1")
	compile(group = "com.github.ice1k", name = "darcula", version = "2018.1")
	compile(group = "com.intellij", name = "forms_rt", version = "7.0.3")
	compile(files(Paths.get("lib", "filedrop.jar")))
	configurations.compileOnly.exclude(group = "com.jetbrains", module = "ideaLocal")
	compileOnly(files(Paths.get("lib", "AppleJavaExtensions-1.6.jar")))
	configurations.runtime.extendsFrom(configurations.testCompileOnly)
	testCompile(project(":common"))
	testCompile("junit", "junit", "4.12")
	testCompile(kotlin("test-junit", kotlinStable))
	testCompile(kotlin("stdlib-jdk8", kotlinStable))
}
