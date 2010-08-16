package org.sonatype.nexus.formfields;

public class StringTextFormField
    extends AbstractFormField
{
    public StringTextFormField( String id, String label, String helpText, boolean required, String regexValidation )
    {
        super( id, label, helpText, required, regexValidation );
    }
    
    public StringTextFormField( String id, String label, String helpText, boolean required )
    {
        super( id, label, helpText, required );
    }
    
    public StringTextFormField( String id )
    {
        super( id );
    }
    
    public String getType()
    {
        return "string";
    }
}
