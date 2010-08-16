package org.sonatype.nexus.formfields;

public interface FormField
{
    public static final boolean MANDATORY = true;
    public static final boolean OPTIONAL = false;
    
    /**
     * Get the type of this form field
     * @return
     */
    String getType();
    
    /**
     * Get the label of this form field
     * @return
     */
    String getLabel();
    
    /**
     * Get the ID of this form field
     * @return
     */
    String getId();

    /**
     * get the required flag of this field
     * @return
     */
    boolean isRequired();

    /**
     * Get the help text of this field
     * @return
     */
    String getHelpText();

    /**
     * Get the regexvalidation of this field
     * @return
     */
    String getRegexValidation();
}
