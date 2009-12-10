package org.sonatype.nexus.buup.api.dto;

/**
 * This is the DTO that carries the form data that user filled in as very first step of upgrade.
 * 
 * @author cstamas
 */
public class UpgradeFormRequest
{
    private boolean acceptsTermsAndConditions;

    private String name;

    private String email;

    // BUUP options

    private int xmxInMbs;

    private int xmsInMbs;

    public boolean isAcceptsTermsAndConditions()
    {
        return acceptsTermsAndConditions;
    }

    public void setAcceptsTermsAndConditions( boolean acceptsTermsAndConditions )
    {
        this.acceptsTermsAndConditions = acceptsTermsAndConditions;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public int getXmxInMbs()
    {
        return xmxInMbs;
    }

    public void setXmxInMbs( int xmxInMbs )
    {
        this.xmxInMbs = xmxInMbs;
    }

    public int getXmsInMbs()
    {
        return xmsInMbs;
    }

    public void setXmsInMbs( int xmsInMbs )
    {
        this.xmsInMbs = xmsInMbs;
    }

}
