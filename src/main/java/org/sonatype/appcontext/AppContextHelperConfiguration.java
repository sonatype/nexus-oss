package org.sonatype.appcontext;

public class AppContextHelperConfiguration
{
    /**
     * The key used for looking into System properties to get the basedir value.
     */
    private String basedirPropertyKey = "basedir";

    public String getBasedirPropertyKey()
    {
        return basedirPropertyKey;
    }

    public void setBasedirPropertyKey( String basedirPropertyKey )
    {
        this.basedirPropertyKey = basedirPropertyKey;
    }
}
