package org.sonatype.nexus.formfields;

public class RepoComboFormField
    extends AbstractFormField
{
    public static final String DEFAULT_HELP_TEXT = "Select the repository or repository group.";

    public static final String DEFAULT_LABEL = "Repository";

    public RepoComboFormField( String id, String label, String helpText, boolean required, String regexValidation )
    {
        super( id, label, helpText, required, regexValidation );
    }

    public RepoComboFormField( String id, String label, String helpText, boolean required )
    {
        super( id, label, helpText, required );
    }

    public RepoComboFormField( String id, boolean required )
    {
        super( id, DEFAULT_LABEL, DEFAULT_HELP_TEXT, required );
    }

    public RepoComboFormField( String id )
    {
        super( id, DEFAULT_LABEL, DEFAULT_HELP_TEXT, false );
    }

    public String getType()
    {
        return "repo";
    }
}
