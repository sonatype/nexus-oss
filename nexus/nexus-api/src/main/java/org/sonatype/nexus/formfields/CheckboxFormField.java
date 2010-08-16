package org.sonatype.nexus.formfields;

public class CheckboxFormField
    extends AbstractFormField
{   
    public CheckboxFormField( String id, String label, String helpText, boolean required )
    {
        super( id, label, helpText, required );
    }
    
    public CheckboxFormField( String id )
    {
        super( id );
    }
    
    public String getType()
    {
        return "checkbox";
    }
}
