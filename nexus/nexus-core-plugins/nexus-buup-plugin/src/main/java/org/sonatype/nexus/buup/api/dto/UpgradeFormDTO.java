package org.sonatype.nexus.buup.api.dto;

import org.sonatype.nexus.buup.invoke.NexusBuupInvocationRequest;

import com.thoughtworks.xstream.annotations.XStreamAlias;
@XStreamAlias( value = "upgradeForm" )
public class UpgradeFormDTO
{
    private boolean agreeLicenseAgreement;

    private String firstName;

    private String lastName;

    private String title;

    private String email;

    private String phone;

    private String organization;

    private String streetAddress;

    private String city;

    private String state;

    private String zipCode;

    private String country;

    private int xmxInMbs = NexusBuupInvocationRequest.XM_UNCHANGED;

    private int xmsInMbs = NexusBuupInvocationRequest.XM_UNCHANGED;

    public boolean isAgreeLicenseAgreement()
    {
        return agreeLicenseAgreement;
    }

    public void setAgreeLicenseAgreement( boolean agreeLicenseAgreement )
    {
        this.agreeLicenseAgreement = agreeLicenseAgreement;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName( String firstName )
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName( String lastName )
    {
        this.lastName = lastName;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle( String title )
    {
        this.title = title;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone( String phone )
    {
        this.phone = phone;
    }

    public String getOrganization()
    {
        return organization;
    }

    public void setOrganization( String orgnization )
    {
        this.organization = orgnization;
    }

    public String getStreetAddress()
    {
        return streetAddress;
    }

    public void setStreetAddress( String streetAddress )
    {
        this.streetAddress = streetAddress;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity( String city )
    {
        this.city = city;
    }

    public String getState()
    {
        return state;
    }

    public void setState( String state )
    {
        this.state = state;
    }

    public String getZipCode()
    {
        return zipCode;
    }

    public void setZipCode( String zipCode )
    {
        this.zipCode = zipCode;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry( String country )
    {
        this.country = country;
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
