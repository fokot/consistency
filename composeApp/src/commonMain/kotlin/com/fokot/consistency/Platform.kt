package com.fokot.consistency

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform