package org.sonatype.nexus.plugins.capabilities.api.descriptor;

public interface CapabilityPropertyDescriptor
{

    String id();

    String name();

    String type();

    boolean isRequired();

    String regexValidation();

}