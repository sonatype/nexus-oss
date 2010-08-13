package org.sonatype.nexus.formfields;

public interface FormField
{
    //These are IMMUTABLE fields
    /**
     * Get the type of this form field
     * @return
     */
    String getType();
    
    //These are MUTABLE fields
    /**
     * Get the label of this form field
     * @return
     */
    String getLabel();
    void setLabel( String label );
    
    /**
     * Get the ID of this form field
     * @return
     */
    String getId();
    void setId( String id );

    /**
     * get the required flag of this field
     * @return
     */
    boolean isRequired();
    void setRequired( boolean required );

    /**
     * Get the help text of this field
     * @return
     */
    String getHelpText();
    void setHelpText( String helpText );

    /**
     * Get the regexvalidation of this field
     * @return
     */
    String getRegexValidation();
    void setRegexValidation( String regex );
}
