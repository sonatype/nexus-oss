package org.sonatype.nexus.plugins.filter;

public enum DependencyResolution
{
    /** The dependency is just fine, and is added to plugin Classloader */
    ACCEPTED,
    
    /** The same dependency is found in core, and it is NOT added to plugin Classpath, will be accessible to plugin from parent Classloader */
    BANNED,
    
    /** The dependency is clashing with some other dependecy in core, this will ban the plugin too unless forced */ 
    CLASHED;
}
