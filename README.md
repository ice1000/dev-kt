# DevKt

[![](https://jitpack.io/v/ice1000/dev-kt.svg)](https://jitpack.io/#ice1000/dev-kt)

CI|Status
:---:|:---:
Travis CI (test, no artifact)|[![Build Status](https://travis-ci.org/ice1000/dev-kt.svg?branch=master)](https://travis-ci.org/ice1000/dev-kt)
AppVeyor (artifact, no test)|[![Build status](https://ci.appveyor.com/api/projects/status/c0aq16ej7415m302?svg=true)](https://ci.appveyor.com/project/ice1000/dev-kt)
CircleCI (both)|[![CircleCI](https://circleci.com/gh/ice1000/dev-kt.svg?style=svg)](https://circleci.com/gh/ice1000/dev-kt)

This is a DevCpp-like cross-platform Kotlin (and Java, experimental) IDE features in lightweight.

You can download a snapshot [here](https://ci.appveyor.com/project/ice1000/dev-kt/build/artifacts), the one ends with "-all.jar" is an executable jar.

Here are some screenshots:

![devkt-2](https://user-images.githubusercontent.com/16398479/39244656-e0537d3a-48c3-11e8-8c52-301e11dd204d.gif)
<img src="https://user-images.githubusercontent.com/16398479/38440232-5ab4d282-3a13-11e8-9b00-5d199d687f8f.png">
<img src="https://user-images.githubusercontent.com/16398479/38440305-983541b4-3a13-11e8-9651-25e9a61a9b9a.png">

# Features

+ Fast (at least faster than Emacs/Eclipse/IntelliJ/CLion/VSCode/Atom)
+ Lightweight (Just a tiny Java Swing application)
+ Kotlin compiler integration (**100% correct parsing**)
+ JetBrains IDE icons
+ Build as jar/class files, run after build, just one click
+ Cross platform (windows/macos/linux), just an executable jar
+ One property-based configuration file, hackable
+ Experimental Java support
+ Plugin system based on `ServiceLoader`

Just a simple comparison:

DevKt<br/><br/>Correct|<img width=200 src="https://user-images.githubusercontent.com/16398479/38292932-3c4ce2be-3818-11e8-9a56-9d30f3109c43.png">
:---:|:---:
IntelliJ IDEA<br/><br/>Correct,<br/>with inspections|<img width=200 src="https://user-images.githubusercontent.com/16398479/38292918-2ec81974-3818-11e8-8eb7-3648cd747ee5.png">
Emacs<br/><br/>Incorrect|<img width=200 src="https://user-images.githubusercontent.com/16398479/38292966-6670c57e-3818-11e8-8a26-3eccf864b93e.png">
VSCode<br/><br/>Incorrect|<img width=200 src="https://user-images.githubusercontent.com/16398479/38293034-95d721be-3818-11e8-9141-19faabae161e.png">

# For Linux users

To use the JavaFX version on Linux, please install oraclejdk instead of openjdk:

```
$ sudo add-apt-repository ppa:webupd8team/java
$ sudo apt-get update
$ sudo apt-get install oracle-java8-installer
```

# Plugin development guide

See https://devkt-plugins.github.io

To install a plugin, just add the jar in the classpath, and you don't need to do anything else.
