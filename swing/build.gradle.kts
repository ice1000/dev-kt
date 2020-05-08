import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

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

repositories { maven("https://jitpack.io") }

dependencies {
	implementation(project(":common"))
	implementation(group = "com.github.cqjjjzr", name = "Gensokyo", version = "1.2.2")
	implementation(group = "net.iharder.dnd", name = "filedrop", version = "2018.1")
	compileOnly(files("lib/AppleJavaExtensions-1.6.jar"))
	// configurations.runtimeClasspath.extendsFrom(configurations.testCompileOnly)
	testImplementation(project(":common"))
}

task<Jar>("fatJar") {
	archiveClassifier.set("all")
	description = "Assembles a jar archive containing the main classes and all the dependencies."
	group = "build"
	from(configurations.runtimeClasspath.get()
			.filter { it.parentFile.name != "plugins" }
			.map { if (it.isDirectory) it as Any else zipTree(it) })
	with(tasks["jar"] as Jar)
}

tasks.withType<Jar> {
	manifest {
		attributes(mapOf("Main-Class" to application.mainClassName,
				"SplashScreen-Image" to "icon/Icon.png"))
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
		resources.setSrcDirs(emptyList<Any>())
		java.setSrcDirs(emptyList<Any>())
		withConvention(KotlinSourceSet::class) {
			kotlin.setSrcDirs(listOf("test"))
		}
	}
}
