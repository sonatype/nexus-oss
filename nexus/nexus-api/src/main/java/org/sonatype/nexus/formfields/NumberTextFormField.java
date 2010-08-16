package org.sonatype.nexus.formfields;

public class NumberTextFormField
    extends AbstractFormField
{
    public NumberTextFormField( String id, String label, String helpText, boolean required, String regexValidation )
    {
        super( id, label, helpText, required, regexValidation );
    }
    
    public NumberTextFormField( String id, String label, String helpText, boolean required )
    {
        super( id, label, helpText, required );
    }
    
    public NumberTextFormField( String id )
    {
        super( id );
    }
    
    public String getType()
    {
        return "number";
    }
}
