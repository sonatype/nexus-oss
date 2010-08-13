package org.sonatype.nexus.test;

import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityPropertyDescriptor;

public class StringCapabilityPropertyDescriptor
    implements CapabilityPropertyDescriptor
{

    private boolean required;

    private String name;

    private String id;

    private String regex;

    public StringCapabilityPropertyDescriptor( String id, String name, boolean required )
    {
        this( id, name, required, null );
    }

    public StringCapabilityPropertyDescriptor( String id, String name, boolean required, String regex )
    {
        super();
        this.id = id;
        this.name = name;
        this.required = required;
        this.regex = regex;
    }

    public String id()
    {
        return id;
    }

    public String name()
    {
        return name;
    }

    public String type()
    {
        return "string";
    }

    public boolean isRequired()
    {
        return required;
    }

    public String regexValidation()
    {
        return regex;
    }

}
