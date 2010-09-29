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

    /**
     * The profile mode
     */
    private String mode;

    public StageProfile( String profileId, String name )
    {
        this( profileId, name, null );
    }

    public StageProfile( String profileId, String name, String mode )
    {
        super();
        this.profileId = profileId;
        this.name = name;
        this.mode = mode;
    }

    public String getProfileId()
    {
        return profileId;
    }

    public String getName()
    {
        return name;
    }

    public String getMode()
    {
        return mode;
    }

}
