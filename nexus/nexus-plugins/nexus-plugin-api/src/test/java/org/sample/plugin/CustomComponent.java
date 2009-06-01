package org.sample.plugin;

import org.sonatype.plexus.plugin.Managed;

@Managed
public interface CustomComponent
{
    String sayHello();
}
