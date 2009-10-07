package org.sonatype.nexus.plugins;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface (must extends org.sonatype.nexus.proxy.repository.Repository) as new repository type to be
 * handled by Nexus.
 * 
 * @author cstamas
 */
@Documented
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface RepositoryType
{
    /**
     * The path prefix to "mount" under content URL.
     */
    String pathPrefix();
}
