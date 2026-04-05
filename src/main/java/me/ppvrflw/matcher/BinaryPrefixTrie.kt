package me.ppvrflw.matcher

private const val STRIDE = 4
private const val CHILDREN = 1 shl STRIDE // 16
private const val STRIDE_MASK = CHILDREN - 1 // 0xF

/**
 * A generic multi-bit trie for prefix matching.
 *
 * Uses a 4-bit stride (16 children per node) to reduce tree depth. IPv4/32 traverses 8 nodes;
 * IPv6/128 traverses 32 nodes.
 *
 * Prefixes that don't align to the stride boundary are handled by fanning out the partial stride
 * across all matching children on insert/remove.
 *
 * @param V The type of values stored at trie nodes.
 */
class BinaryPrefixTrie<V> {

  private class Node<V> {
    val children = arrayOfNulls<Node<V>>(CHILDREN)
    val values: MutableList<V> = mutableListOf()
  }

  private val root = Node<V>()

  fun insert(tokens: LongArray, prefixLength: Int, value: V) {
    val node = walkOrCreate(tokens, prefixLength / STRIDE)
    fanout(node, tokens, prefixLength) { it.values.add(value) }
  }

  fun remove(tokens: LongArray, prefixLength: Int, value: V) {
    val node = walk(tokens, prefixLength / STRIDE) ?: return
    fanout(node, tokens, prefixLength) { it.values.remove(value) }
  }

  fun match(tokens: LongArray, prefixLength: Int): List<V> = buildList {
    var node = root
    repeat(prefixLength / STRIDE) { i ->
      addAll(node.values)
      node = node.children[tokens.nibbleAt(i)] ?: return@buildList
    }
    addAll(node.values)
  }

  private fun walk(tokens: LongArray, strides: Int): Node<V>? {
    var node = root
    repeat(strides) { i -> node = node.children[tokens.nibbleAt(i)] ?: return null }
    return node
  }

  private fun walkOrCreate(tokens: LongArray, strides: Int): Node<V> {
    var node = root
    repeat(strides) { i ->
      val nibble = tokens.nibbleAt(i)
      node = node.children[nibble] ?: Node<V>().also { node.children[nibble] = it }
    }
    return node
  }

  private inline fun fanout(
      node: Node<V>,
      tokens: LongArray,
      prefixLength: Int,
      action: (Node<V>) -> Unit,
  ) {
    val remainingBits = prefixLength % STRIDE
    if (remainingBits == 0) {
      action(node)
    } else {
      val shift = STRIDE - remainingBits
      val partial = tokens.nibbleAt(prefixLength / STRIDE) ushr shift
      for (suffix in 0 until (1 shl shift)) {
        val nibble = (partial shl shift) or suffix
        val child = node.children[nibble] ?: Node<V>().also { node.children[nibble] = it }
        action(child)
      }
    }
  }
}

private fun LongArray.nibbleAt(strideIndex: Int): Int {
  val bitIndex = strideIndex * STRIDE
  return ((this[bitIndex ushr 6] ushr (60 - (bitIndex and 63))) and STRIDE_MASK.toLong()).toInt()
}
