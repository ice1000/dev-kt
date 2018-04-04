# DevKt

CI|Status
:---:|:---:
Travis CI|[![Build Status](https://travis-ci.org/ice1000/dev-kt.svg?branch=master)](https://travis-ci.org/ice1000/dev-kt)
AppVeyor|[![Build status](https://ci.appveyor.com/api/projects/status/c0aq16ej7415m302?svg=true)](https://ci.appveyor.com/project/ice1000/dev-kt)
CircleCI|[![CircleCI](https://circleci.com/gh/ice1000/dev-kt.svg?style=svg)](https://circleci.com/gh/ice1000/dev-kt)

This is a DevCpp-like cross-platform Kotlin IDE features in lightweight.

# Features

+ Fast (at least faster than Emacs/Eclipse/IntelliJ/CLion/VSCode/Atom)
+ Lightweight (Just a tiny Java Swing application)
+ Kotlin compiler integration (**100% correct parsing**)
+ JetBrains IDE icons
+ Build as jar/class files, run after build, just one click
+ Cross platform (windows/macos/linux), just one jar file

Just a simple comparison:

DevKt<br/><br/>Correct|<img width=200 src="https://user-images.githubusercontent.com/16398479/38292932-3c4ce2be-3818-11e8-9a56-9d30f3109c43.png">
:---:|:---:
IntelliJ IDEA<br/><br/>Correct,<br/>with inspections|<img width=200 src="https://user-images.githubusercontent.com/16398479/38292918-2ec81974-3818-11e8-8eb7-3648cd747ee5.png">
Emacs<br/><br/>Incorrect|<img width=200 src="https://user-images.githubusercontent.com/16398479/38292966-6670c57e-3818-11e8-8a26-3eccf864b93e.png">
VSCode<br/><br/>Incorrect|<img width=200 src="https://user-images.githubusercontent.com/16398479/38293034-95d721be-3818-11e8-9141-19faabae161e.png">

# Progress

+ Properties-based settings (hackable!)
	+ [X] Deals with missing properties
	+ [X] Smart auto saving
	+ [X] Highlight color customization
	+ [X] Hot reload
	+ [ ] Text style customization
	+ [ ] Font customization
	+ [ ] Keymap customization
	+ [ ] Internationalization
	+ [ ] Settings UI
+ File operations
	+ [X] Create new file when no files are opened
	+ [X] Save and sync
	+ [X] Show in files
	+ [X] Record recently opened files
	+ [X] Create new files from templates
	+ [ ] Drag files to open
+ [ ] Editing
	+ [X] Lexer based highlights
	+ [X] Undo and redo (by `javax.swing.undo.UndoManager.UndoManager`)
	+ [X] Copy/paste/cut/select all
	+ [X] Semantic-based highlights
		+ [ ] Highlight in daemon
		+ [ ] Incremental
	+ [ ] Highlight selected token
	+ [ ] Auto indent
		+ [ ] Smart indent
		+ [ ] Indent with spaces
+ [ ] Build and execution
	+ [ ] Build as class files
	+ [ ] Build as jar
	+ [ ] Build and run
	+ [ ] Run as class files
	+ [ ] Run as jar
	+ [ ] Run as kotlin script
+ Others
	+ [X] Open alternative editors' download page in browser
	+ [X] MacOS toolbar support
	+ [ ] Built-in documentation

# Build

+ (Optional) Download and decompress [Sarasa Gothic](https://github.com/be5invis/Sarasa-Gothic/releases) font to `res/font`
  + As reference you can see [this shell script](./download-font.sh)
+ Use `gradlew run` to run this application
