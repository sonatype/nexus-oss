package org.sonatype.nexus.formfields;

public class RepoOrGroupComboFormField
    extends AbstractFormField
{
    private static final String DEFAULT_HELP_TEXT = "Select the repository or repository group to which this capability applies";
    private static final String DEFAULT_LABEL = "Repository/Group"; 
    public RepoOrGroupComboFormField( String id, String label, String helpText, boolean required, String regexValidation )
    {
        super( id, label, helpText, required, regexValidation );
    }
    
    public RepoOrGroupComboFormField( String id, String label, String helpText, boolean required )
    {
        super( id, label, helpText, required );
    }
    
    public RepoOrGroupComboFormField( String id, boolean required )
    {
        super( id, DEFAULT_LABEL, DEFAULT_HELP_TEXT, required );
    }
    
    public RepoOrGroupComboFormField( String id )
    {
        super( id, DEFAULT_LABEL, DEFAULT_HELP_TEXT, false );
    }
    
    public String getType()
    {
        return "repo-or-group";
    }
}
