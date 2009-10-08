package org.sonatype.nexus.proxy.events;

public interface VetoFormatter
{
    String format( VetoFormatterRequest request );
}
