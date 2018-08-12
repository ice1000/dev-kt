import de.undercouch.gradle.tasks.download.Download
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
	java
	maven
	id("de.undercouch.download") version "3.4.2"
	kotlin("jvm")
}

java.sourceSets {
	"main" {
		resources.srcDir("res")
		java.srcDir("src")
		withConvention(KotlinSourceSet::class) { kotlin.srcDir("src") }
	}

	"test" {
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
	compile(kotlin("stdlib-jdk8", kotlinVersion))
	compile(kotlin("reflect", kotlinVersion))
	compile(kotlin("compiler-embeddable", kotlinVersion))
	compile(kotlin("script-util", kotlinVersion))

	// for the icon loader
	compile(group = "org.ice1000.textseq", name = "impl-gap", version = "v0.3")
	compile(group = "com.bulenkov", name = "darcula", version = "2018.2")
	compile(group = "com.bennyhuo.kotlin", name = "opd", version = "1.0-rc-2")
	compileOnly(files(*file("lib").listFiles().orEmpty()))
	val plugins = file("plugins").listFiles().orEmpty().filterNot { it.isDirectory }
	runtime(files(*plugins.toTypedArray()))
	testCompile(group = "junit", name = "junit", version = "4.12")
	testCompile(kotlin("test-junit", kotlinStable))
	testCompile(kotlin("stdlib-jdk8", kotlinStable))
	testCompile(kotlin("reflect", kotlinStable))
	configurations.runtime.extendsFrom(configurations.testCompileOnly)
}
