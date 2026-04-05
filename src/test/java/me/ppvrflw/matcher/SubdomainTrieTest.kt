package me.ppvrflw.matcher

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withTests
import io.kotest.matchers.shouldBe

class SubdomainTrieTest : FunSpec({

    data class SingleInsertCase(
        val name: String,
        val parts: List<String>,
        val value: String,
        val query: List<String>,
        val expected: List<String>
    )

    context("simple tests") {
        withTests(
            nameFn = { it.name },
            SingleInsertCase(
                name = "hostname with 1 subdomain",
                parts = listOf("sub"),
                value = "sub.example.com",
                query = listOf("sub"),
                expected = listOf("sub.example.com")
            ), SingleInsertCase(
                name = "hostname with two subdomains",
                parts = listOf("sub", "api"),
                value = "sub.api.example.com",
                query = listOf("sub", "api"),
                expected = listOf("sub.api.example.com")
            ), SingleInsertCase(
                name = "child query matches parent entry",
                parts = listOf("sub"),
                value = "sub.example.com",
                query = listOf("api", "sub"),
                expected = listOf("sub.example.com")
            ), SingleInsertCase(
                name = "deeply nested subdomain",
                parts = listOf("a", "b", "c", "d"),
                value = "deep",
                query = listOf("a", "b", "c", "d"),
                expected = listOf("deep")
            ), SingleInsertCase(
                name = "no match returns empty",
                parts = listOf("sub"),
                value = "sub.example.com",
                query = listOf("api"),
                expected = emptyList()
            ), SingleInsertCase(
                name = "empty query returns empty",
                parts = listOf("sub"),
                value = "sub.example.com",
                query = emptyList(),
                expected = emptyList()
            ), SingleInsertCase(
                name = "current == null",
                parts = listOf("www", "example", "com"),
                value = "www.example.com",
                query = listOf("www", "foo", "com"),
                expected = emptyList()
            )
        ) { (_, parts, value, query, expected) ->
            val trie = SubdomainTrie<String>().apply {
                insert(parts, value)
            }
            trie.match(query) shouldBe expected
        }
    }

    test("two values on the same subdomain") {
        val trie = SubdomainTrie<String>().apply {
            insert(listOf("sub"), "sub.api.example.com - phishing")
            insert(listOf("sub"), "sub.api.example.com - spam")
        }

        trie.match(listOf("sub")) shouldBe listOf(
            "sub.api.example.com - phishing", "sub.api.example.com - spam"
        )
    }

    test("inserting same value twice deduplicates") {
        val trie = SubdomainTrie<String>().apply {
            insert(listOf("sub"), "sub.example.com")
            insert(listOf("sub"), "sub.example.com")
        }

        trie.match(listOf("sub")) shouldBe listOf("sub.example.com")
    }

    test("dataclass value deduplication") {
        data class Indicator(val name: String)

        val trie = SubdomainTrie<Indicator>().apply {
            insert(listOf("sub"), Indicator("sub.example.com"))
            insert(listOf("sub"), Indicator("sub.example.com"))
        }

        trie.match(listOf("sub")) shouldBe listOf(Indicator("sub.example.com"))
    }

    test("sibling subdomains match independently") {
        val trie = SubdomainTrie<String>().apply {
            insert(listOf("api"), "api.example.com")
            insert(listOf("rss"), "rss.example.com")
        }

        trie.match(listOf("api")) shouldBe listOf("api.example.com")
        trie.match(listOf("rss")) shouldBe listOf("rss.example.com")
    }

    test("shared prefix subdomains match correctly") {
        val trie = SubdomainTrie<String>().apply {
            insert(listOf("v1", "api"), "v1.api.example.com")
            insert(listOf("v2", "api"), "v2.api.example.com")
        }

        trie.match(listOf("v1", "api")) shouldBe listOf("v1.api.example.com")
        trie.match(listOf("v2", "api")) shouldBe listOf("v2.api.example.com")
    }

    test("parent match does not include child") {
        val trie = SubdomainTrie<String>().apply {
            insert(listOf("sub"), "sub.example.com")
            insert(listOf("api", "sub"), "api.sub.example.com")
        }

        trie.match(listOf("sub")) shouldBe listOf("sub.example.com")
    }

    test("null input returns empty list") {
        val trie = SubdomainTrie<String>().apply {
            insert(listOf("sub"), "sub.example.com")
        }

        trie.match(null) shouldBe emptyList()
    }
})
