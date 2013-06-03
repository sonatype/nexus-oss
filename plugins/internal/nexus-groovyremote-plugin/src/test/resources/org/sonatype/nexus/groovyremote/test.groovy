//package org.sonatype.nexus.groovyremote

// script must define a single closure
def task = { name ->
    println "${Thread.currentThread().name} -> sup $name"
    return 12345
}