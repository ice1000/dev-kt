
# Progress

+ Properties-based settings (hackable!)
	+ [X] Deals with missing properties
	+ [X] Smart auto saving
	+ [X] Highlight color customization
	+ [X] Hot reload (Due to some limitations of Swing it's really hard to implement)
	+ [X] Font customization
		+ [X] Font size customization
	+ [ ] Text style customization
	+ [ ] Keymap customization
	+ [ ] Internationalization
	+ [ ] Settings UI
+ File operations
	+ [X] Create new file when no files are opened
	+ [X] Save and sync
	+ [X] Show in files
	+ [X] Record recently opened files
	+ [X] Create new files from templates (kts, kt, kt2js, android activity, multiplatform codes, etc.)
	+ [X] Drag file to open
+ Editor
	+ [X] Multi-language support
	+ [X] Undo and redo (by `javax.swing.undo.UndoManager.UndoManager`)
	+ [X] Copy/paste/cut/select all
	+ [X] Line number
	+ [X] Background image
	+ [X] Insert/delete paired characters
	+ Highlighting strategy
		+ [ ] Highlight in daemon
		+ [ ] Prioritized
		+ [ ] Incremental
	+ [ ] Highlight selected token
	+ Kotlin
		+ [X] Lexer based highlights
		+ [X] Semantic-based highlights
	+ Java
		+ [X] Lexer based highlights
		+ [X] Semantic-based highlights
	+ [ ] Auto indent
		+ [ ] Smart indent
		+ [ ] Indent with spaces
+ Plugin system
	+ [X] Load plugins in classpath
	+ [X] [Official CovScript plugin](https://github.com/covscript/covscript-devkt)
	+ [X] [Official Clojure plugin based on Clojure-Kit](https://github.com/devkt-plugins/clojure-devkt) (deprecated)
	+ [X] [Official Clojure plugin based on la-clojuer](https://github.com/devkt-plugins/la-clojure-devkt)
	+ [X] [Official Julia plugin](https://github.com/devkt-plugins/julia-devkt)
	+ [X] [Official JSON plugin](https://github.com/devkt-plugins/json-devkt)
	+ [ ] Official Zig plugin
	+ [ ] Official Lua plugin based on EmmyLua
	+ [ ] Official Python plugin
	+ [ ] Official Groovy plugin
	+ [ ] Official Ruby plugin
	+ [ ] Official HTML plugin
	+ [ ] Official XML plugin
	+ [ ] Official CSS plugin
	+ [ ] Official Lice plugin
	+ [ ] Official Markdown plugin
	+ [ ] Official Scala plugin
+ Build and run (Kotlin specific)
	+ Build
		+ [X] Build as class files
		+ [X] Build as jar
		+ [ ] Build as javascript module
	+ Run
		+ [X] Run as class files
		+ [X] Run as jar
		+ [ ] Run as kotlin script
+ Others
	+ [X] Open alternative editors' download page in browser
	+ [X] MacOS toolbar support
	+ [X] PsiViewer
	+ [X] Memory indicator
	+ [ ] Built-in documentation
