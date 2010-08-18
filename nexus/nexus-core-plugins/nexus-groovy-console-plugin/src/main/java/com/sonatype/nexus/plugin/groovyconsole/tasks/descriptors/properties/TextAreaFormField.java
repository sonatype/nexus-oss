package com.sonatype.nexus.plugin.groovyconsole.tasks.descriptors.properties;

import org.sonatype.nexus.formfields.AbstractFormField;

public class TextAreaFormField
    extends AbstractFormField
{
    public TextAreaFormField( String id, String label, String helpText, boolean required, String regexValidation )
    {
        super( id, label, helpText, required, regexValidation );
    }

    public TextAreaFormField( String id, String label, String helpText, boolean required )
    {
        super( id, label, helpText, required );
    }

    public TextAreaFormField( String id )
    {
        super( id );
    }

    public String getType()
    {
        return "textarea";
    }
}
