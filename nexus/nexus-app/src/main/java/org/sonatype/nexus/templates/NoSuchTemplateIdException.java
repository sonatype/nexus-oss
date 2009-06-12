package org.sonatype.nexus.templates;

public class NoSuchTemplateIdException
    extends Exception
{
    private static final long serialVersionUID = -4285603086224574998L;

    public NoSuchTemplateIdException( String message )
    {
        super( message );
    }
}
