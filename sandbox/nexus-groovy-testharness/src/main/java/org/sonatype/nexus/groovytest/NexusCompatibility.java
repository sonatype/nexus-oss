package org.sonatype.nexus.groovytest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.METHOD )
public @interface NexusCompatibility
{

    String minVersion() default "";

    String maxVersion() default "";

}
