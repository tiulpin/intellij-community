// "Replace with dot call" "true"

fun <T : Any> foo(x: T) {
    val y = x?.toString()<caret>
}
/* IGNORE_FIR */
