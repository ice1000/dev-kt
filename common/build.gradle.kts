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
	val kotlinVersion: String by rootProject.extra
	implementation(kotlin("stdlib-jdk8", kotlinVersion))
	implementation(kotlin("reflect", kotlinVersion))
	implementation(kotlin("compiler-embeddable", kotlinVersion))
	implementation(kotlin("script-util", kotlinVersion))

	// for the icon loader
	implementation(group = "org.ice1000.textseq", name = "impl-gap", version = "v0.3")
	implementation(group = "com.bulenkov", name = "darcula", version = "2018.2")
	implementation(group = "com.bennyhuo.kotlin", name = "opd", version = "1.0-rc-2")
	compileOnly(files(*file("lib").listFiles().orEmpty()))
	val plugins = file("plugins").listFiles().orEmpty().filterNot { it.isDirectory }
	runtimeClasspath(files(*plugins.toTypedArray()))
	testImplementation(group = "junit", name = "junit", version = "4.12")
	testImplementation(kotlin("test-junit", kotlinStable))
	testImplementation(kotlin("stdlib-jdk8", kotlinStable))
	testImplementation(kotlin("reflect", kotlinStable))
	// configurations.runtimeClasspath.extendsFrom(configurations.testCompileOnly)
}
