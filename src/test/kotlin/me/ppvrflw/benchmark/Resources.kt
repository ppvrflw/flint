package me.ppvrflw.benchmark

fun resourceLines(name: String): List<String> =
    object {}::class.java.classLoader.getResourceAsStream(name)!!.bufferedReader().readLines()
