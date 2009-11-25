package org.sonatype.sample.wrapper;


/**
 * A WrapperHelper configuration providing all the defaults for Sonatype Application Bundles, but still making those
 * overridable.
 * 
 * @author cstamas
 */
public class WrapperHelperConfiguration
{
    /**
     * The key used for looking into System properties to get the basedir value.
     */
    private String basedirPropertyKey = "basedir";

    /**
     * The path of the "conf" directory. If relative, it is calculated from basedir.
     */
    private String confDirPath = "conf";

    /**
     * The name of the wrapper.conf file.
     */
    private String wrapperConfName = "wrapper.conf";

    /**
     * The name of the backup of the wrapper.conf file.
     */
    private String wrapperConfBackupName = "wrapper.conf.bak";

    public String getBasedirPropertyKey()
    {
        return basedirPropertyKey;
    }

    public void setBasedirPropertyKey( String basedirPropertyKey )
    {
        this.basedirPropertyKey = basedirPropertyKey;
    }

    public String getConfDirPath()
    {
        return confDirPath;
    }

    public void setConfDirPath( String confDirPath )
    {
        this.confDirPath = confDirPath;
    }

    public String getWrapperConfName()
    {
        return wrapperConfName;
    }

    public void setWrapperConfName( String wrapperConfName )
    {
        this.wrapperConfName = wrapperConfName;
    }

    public String getWrapperConfBackupName()
    {
        return wrapperConfBackupName;
    }

    public void setWrapperConfBackupName( String wrapperConfBackupName )
    {
        this.wrapperConfBackupName = wrapperConfBackupName;
    }
}
