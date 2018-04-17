import de.undercouch.gradle.tasks.download.Download
import org.jetbrains.kotlin.com.intellij.openapi.util.SystemInfo
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.util.concurrent.*

val commitHash: String by rootProject.extra
val isCI: Boolean by rootProject.extra

plugins {
	java
	application
	id("de.undercouch.download") version "3.4.2"
	kotlin("jvm")
}

application {
	if (SystemInfo.isMac)
		applicationDefaultJvmArgs = listOf("-Xdock:name=Dev-Kt")
	mainClassName = "org.ice1000.devkt.Main"
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

tasks.withType<Jar> {
	manifest {
		attributes(mapOf("Main-Class" to application.mainClassName,
				"SplashScreen-Image" to "icon/kotlin@288x288.png"))
	}
}

val downloadFiraCode = task<Download>("downloadFiraCode") {
	src("https://raw.githubusercontent.com/tonsky/FiraCode/master/distr/ttf/FiraCode-Regular.ttf")
	dest(file("res/font").apply { if (!exists()) mkdirs() })
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
		resources.setSrcDirs(emptyList<Any>())
		java.setSrcDirs(emptyList<Any>())
		withConvention(KotlinSourceSet::class) {
			kotlin.setSrcDirs(listOf("test"))
		}
	}
}

dependencies {
	val kotlinStable: String by rootProject.extra
	compile(project(":common"))
	compile(group = "com.github.cqjjjzr", name = "Gensokyo", version = "1.1")
	compile(group = "com.github.ice1k", name = "darcula", version = "2018.2")
	compile(files("lib/filedrop.jar"))
	compileOnly(files("lib/AppleJavaExtensions-1.6.jar"))
	configurations.runtime.extendsFrom(configurations.testCompileOnly)
	testCompile(project(":common"))
	testCompile("junit", "junit", "4.12")
	testCompile(kotlin("test-junit", kotlinStable))
	testCompile(kotlin("stdlib-jdk8", kotlinStable))
}
