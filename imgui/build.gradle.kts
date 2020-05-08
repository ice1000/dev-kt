import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.util.concurrent.*

val commitHash: String by rootProject.extra
val isCI: Boolean by rootProject.extra
val isMac: Boolean by rootProject.extra
val kotlinStable: String by rootProject.extra

plugins {
	java
	application
	kotlin("jvm")
}

application {
	if (isMac) applicationDefaultJvmArgs = listOf("-Xdock:name=Dev-Kt")
	mainClassName = "org.ice1000.devkt.Main"
}

dependencies {
	implementation(project(":common"))
	val jimguiVersion = "v0.9"
	implementation(group = "org.ice1000.jimgui", name = "core", version = jimguiVersion)
	implementation(group = "org.ice1000.jimgui", name = "kotlin-dsl", version = jimguiVersion)
	testImplementation(project(":common"))
	testImplementation("junit", "junit", "4.12")
	testImplementation(kotlin("test-junit", kotlinStable))
	testImplementation(kotlin("stdlib-jdk8", kotlinStable))
}

task<Jar>("fatJar") {
	classifier = "all"
	description = "Assembles a jar archive containing the main classes and all the dependencies."
	group = "build"
	from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it as Any else zipTree(it) })
	with(tasks["jar"] as Jar)
}

tasks.withType<Jar> {
	manifest {
		attributes(mapOf("Main-Class" to application.mainClassName,
				"SplashScreen-Image" to "icon/kotlin@288x288.png"))
	}
}

sourceSets {
	main {
		resources.setSrcDirs(listOf("res"))
		java.setSrcDirs(listOf("src"))
		withConvention(KotlinSourceSet::class) {
			kotlin.setSrcDirs(listOf("src"))
		}
	}

	test {
		resources.setSrcDirs(listOf("testRes"))
		java.setSrcDirs(listOf("test"))
		withConvention(KotlinSourceSet::class) {
			kotlin.setSrcDirs(listOf("test"))
		}
	}
}
