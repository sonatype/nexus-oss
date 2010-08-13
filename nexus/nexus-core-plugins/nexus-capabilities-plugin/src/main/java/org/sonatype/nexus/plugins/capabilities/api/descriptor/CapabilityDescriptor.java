package org.sonatype.nexus.plugins.capabilities.api.descriptor;

import org.sonatype.plugin.ExtensionPoint;

@ExtensionPoint
public interface CapabilityDescriptor
{

    String id();

    String name();

    CapabilityPropertyDescriptor[] propertyDescriptors();

    boolean isExposed();

}