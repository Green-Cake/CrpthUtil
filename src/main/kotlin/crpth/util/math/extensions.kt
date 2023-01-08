package crpth.util.math

@JvmName("sumOfFloat")
inline fun CharSequence.sumOf(selector: (Char)->Float): Float {
    var sum = 0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}