package org.sonatype.nexus.restlight.stage;

/**
 * Simple object to represent a Staging or Build Promotion Profile.
 * 
 * @author Brian Demers
 */
public class StageProfile
{
    /**
     * Id of the profile.
     */
    private String profileId;

    /**
     * Display name of the profile.
     */
    private String name;

    public StageProfile( String profileId, String name )
    {
        super();
        this.profileId = profileId;
        this.name = name;
    }

    public String getProfileId()
    {
        return profileId;
    }

    public String getName()
    {
        return name;
    }

}
