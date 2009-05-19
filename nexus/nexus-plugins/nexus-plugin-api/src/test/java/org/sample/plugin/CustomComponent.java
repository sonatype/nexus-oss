package org.sample.plugin;

import org.sonatype.nexus.plugins.Managed;

@Managed
public interface CustomComponent
{
    String sayHello();
}
