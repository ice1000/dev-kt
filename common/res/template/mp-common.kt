package devkt

expect class Foo(bar: String) {
	fun frob()
}

fun main(args: Array<String>) {
	Foo("Hello").frob()
}
