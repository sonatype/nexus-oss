package org.sonatype.nexus.plugins;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface as Nexus Extension Point. This annotation is meant for "host" (the extensible system/app)
 * developers to mark their extension points.
 * 
 * @author cstamas
 */
@Documented
@Inherited
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface ExtensionPoint
{
    /**
     * Should the extension point component be created as singleton, or per-lookup?
     * 
     * @return
     */
    boolean isSingleton() default true;
}
