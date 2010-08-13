package org.sonatype.nexus.plugins.capabilities.api.descriptor;

import java.util.List;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.plugin.ExtensionPoint;

@ExtensionPoint
public interface CapabilityDescriptor
{

    String id();

    String name();
    
    List<FormField> formFields();

    boolean isExposed();

}