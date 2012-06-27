package org.sonatype.nexus.bundle.launcher;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate test classes for customizing when Nexus is tarted and stopped. Possible options are each
 * method (default) and once per test class.
 *
 * @since 2.1
 */
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
@Inherited
@Documented
public @interface NexusStartAndStopStrategy
{

    Strategy value() default Strategy.EACH_METHOD;

    public static enum Strategy
    {
        EACH_TEST, EACH_METHOD;
    }

}
