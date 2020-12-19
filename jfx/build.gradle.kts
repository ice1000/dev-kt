import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

val commitHash: String by rootProject.extra
val isCI: Boolean by rootProject.extra
val isMac: Boolean by rootProject.extra
val kotlinStable: String by rootProject.extra

plugins {
	java
	application
	kotlin("jvm")
	id ("org.openjfx.javafxplugin") version "0.0.9"
}

application {
	if (isMac) applicationDefaultJvmArgs = listOf("-Xdock:name=Dev-Kt")
	mainClassName = "org.ice1000.devkt.Main"
}

dependencies {
	implementation(project(":common"))
	testImplementation(project(":common"))
}

javafx {
	modules("javafx.controls", "javafx.fxml")
}

task<Jar>("fatJar") {
	archiveClassifier.set("all")
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
