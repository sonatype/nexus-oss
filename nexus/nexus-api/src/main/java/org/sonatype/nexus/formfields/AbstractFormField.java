package org.sonatype.nexus.formfields;

public abstract class AbstractFormField
    implements FormField
{
    private String helpText;
    private String id;
    private String regexValidation;
    private boolean required;
    private String label;
    
    public AbstractFormField( String id, String label, String helpText, boolean required, String regexValidation )
    {
        this( id, label, helpText, required );
        this.regexValidation = regexValidation;
    }
    
    public AbstractFormField( String id, String label, String helpText, boolean required )
    {
        this( id );
        this.label = label;
        this.helpText = helpText;
        this.required = required;
    }
    
    public AbstractFormField( String id )
    {
        this.id = id;
    }
    
    public String getLabel()
    {
        return this.label;
    }
    public String getHelpText()
    {
        return this.helpText;
    }
    public String getId()
    {
        return this.id;
    }
    public String getRegexValidation()
    {
        return this.regexValidation;
    }
    public boolean isRequired()
    {
        return this.required;
    }
    public void setHelpText( String helpText )
    {
        this.helpText = helpText;
    }
    public void setId( String id )
    {
        this.id = id;
    }
    public void setRegexValidation( String regex )
    {
        this.regexValidation = regex;
    }
    public void setRequired( boolean required )
    {
        this.required = required;
    }
    public void setLabel( String label )
    {
        this.label = label;
    }
}
