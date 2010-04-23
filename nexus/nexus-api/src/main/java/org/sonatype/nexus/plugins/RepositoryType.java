package org.sonatype.nexus.plugins;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface (must extends org.sonatype.nexus.proxy.repository.Repository) as new repository type to be handled
 * by Nexus.
 * 
 * @author cstamas
 */
@Documented
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface RepositoryType
{
    /**
     * The constant denoting unlimited count of instances.
     */
    int UNLIMITED_INSTANCES = -1;

    /**
     * The path prefix to "mount" under content URL.
     */
    String pathPrefix();

    /**
     * The "hard" limit of maximal instance count for this repository. Default is unlimited. See NexusConfiguration
     * iface for details.
     */
    int repositoryMaxInstanceCount() default UNLIMITED_INSTANCES;
}
