package com.example.wheresxyz.util

fun sanitizeFirebaseKey(key: String): String {
    return key
        .replace(".", "_")
        .replace("#", "_")
        .replace("$", "_")
        .replace("[", "_")
        .replace("]", "_")
}
