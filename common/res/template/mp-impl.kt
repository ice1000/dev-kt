package devkt

actual class Foo actual constructor(val bar: String) {
	actual fun frob() {
		println("Frobbing the $bar")
	}
}
