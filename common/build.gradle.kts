import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
	java
	maven
	kotlin("jvm")
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
	val kotlinVersion: String by rootProject.extra
	compile(kotlin("stdlib-jdk8", kotlinVersion))
	compile(kotlin("reflect", kotlinVersion))
	compile(kotlin("compiler-embeddable", kotlinVersion))
	compile(kotlin("script-util", kotlinVersion))

	// for the icon loader
	compile(group = "com.github.ice1k", name = "darcula", version = "2018.2")
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
