# How to build

Clone this repo:

```shell
$ git clone https://github.com/ice1000/dev-kt.git
```

Then:

+ (Optional) Download and decompress [Sarasa Gothic](https://github.com/be5invis/Sarasa-Gothic/releases) font to `res/font`
  + As reference you can see [this shell script](./download-font.sh)
+ (Optional) Create `gradle.properties`, set your IntelliJ IDEA installation path to `ideaC_path`
  + If you don't do this, gradle will download an IntelliJ IDEA 2018.1
+ (Optional) Run `gradlew :swing:downloadFiraCode`
  + If you don't do this, the editor font will be extremely ugly
+ Use `gradlew :swing:fatJar` to build this application
+ Run this application with `java -jar swing/build/libs/devkt-[some unimportant text]-all.jar`

BTW if you don't need the "run" function of DevKt,
you can simply run this application by `gradlew :swing:run` after setting up the IntelliJ IDEA path.

# Contributing guidelines

## You must

<!-- 0. Put all natural language strings into the [resource bundle](res/org/ice1000/julia/lang/julia-bundle.properties) -->
0. Use as much `@NotNull` and `@Nullable` as you can in Java codes except local variables

## You must not

0. Break the code style -- use tab indents with spaces aligns
0. Open pull requests just to fix code style, or use some syntax sugar (DevKt is not SharpLang!)
0. Add any kind of generated file into the git repo (including the parser!)
0. Violate the open source license

## You should

0. Use Kotlin except UI, but if you only know Java, never mind, we can help you convert
0. Name your files like `xxx-xxx.kt`
0. Put all highly related classes into a single file
0. Use English, but we also read Chinese so if you only know Chinese just use it
0. Write commit message starting with `[ issue id or refactor type ]`

## You'd better

0. Read http://www.jetbrains.org/display/IJOS/IntelliJ+Coding+Guidelines

## You don't have to

0. Write comments, except you're using magics. Tell us if you do so
0. Write tests, because we'll review your codes carefully


