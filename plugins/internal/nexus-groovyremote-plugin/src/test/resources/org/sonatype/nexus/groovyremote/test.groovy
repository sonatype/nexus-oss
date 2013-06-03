package org.sonatype.nexus.groovyremote

// script must define a single closure
return { name ->
    println "${Thread.currentThread().name} -> sup $name"
    return 12345
}