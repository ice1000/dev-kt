import de.undercouch.gradle.tasks.download.Download
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
	java
	maven
	id("de.undercouch.download")
	kotlin("jvm")
}

sourceSets {
	main {
		resources.srcDir("res")
		java.srcDir("src")
		withConvention(KotlinSourceSet::class) { kotlin.srcDir("src") }
	}

	test {
		resources.srcDir("testRes")
		java.srcDir("test")
		withConvention(KotlinSourceSet::class) { kotlin.srcDir("test") }
	}
}

val downloadFiraCode = task<Download>("downloadFiraCode") {
	src("https://raw.githubusercontent.com/tonsky/FiraCode/master/distr/ttf/FiraCode-Regular.ttf")
	dest(file("res/font"))
	overwrite(false)
}

tasks["processResources"].dependsOn(downloadFiraCode)

dependencies {
	val kotlinStable: String by rootProject.extra

	compileOnly(files(*file("lib").listFiles().orEmpty()))
	val plugins = file("plugins").listFiles().orEmpty().filterNot { it.isDirectory }
	runtimeOnly(files(*plugins.toTypedArray()))
	testImplementation(kotlin("reflect", kotlinStable))
	// configurations.runtimeOnly.extendsFrom(configurations.testCompileOnly)
}
