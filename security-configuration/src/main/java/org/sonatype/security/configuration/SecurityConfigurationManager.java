package org.sonatype.security.configuration;

import java.util.List;

import org.sonatype.configuration.validation.InvalidConfigurationException;

public interface SecurityConfigurationManager
{
    
    void setEnabled(boolean enabled);
    boolean isEnabled();
    
    void setAnonymousAccessEnabled( boolean anonymousAccessEnabled );
    boolean isAnonymousAccessEnabled();
    
    void setAnonymousUsername( String anonymousUsername ) throws InvalidConfigurationException;
    String getAnonymousUsername();
    
    void setAnonymousPassword( String anonymousPassword ) throws InvalidConfigurationException;
    String getAnonymousPassword();
    
    void setRealms( List<String> realms ) throws InvalidConfigurationException;
    List<String> getRealms();
    
    /**
     * Clear the cache and reload from file
     */
    void clearCache();
    
    /**
     * Save to disk what is currently cached in memory 
     */
    void save();

}
