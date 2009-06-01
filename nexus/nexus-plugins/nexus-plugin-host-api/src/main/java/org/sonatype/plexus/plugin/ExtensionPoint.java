package org.sonatype.plexus.plugin;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface as Extension Point (which is implicitly a component contract). This annotation is meant for "host"
 * (the extensible system/app) developers to mark their extension points.
 * 
 * @author cstamas
 */
@Documented
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface ExtensionPoint
{
}
