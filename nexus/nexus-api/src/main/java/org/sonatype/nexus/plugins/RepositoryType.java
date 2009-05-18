package org.sonatype.nexus.plugins;

@ExtensionPoint
public @interface RepositoryType
{
    String pathPrefix() default "";
}
