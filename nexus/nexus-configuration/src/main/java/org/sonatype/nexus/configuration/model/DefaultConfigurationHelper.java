package org.sonatype.nexus.configuration.model;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.PasswordHelper;
import org.sonatype.plexus.components.cipher.PlexusCipherException;

import com.thoughtworks.xstream.XStream;

@Component( role = ConfigurationHelper.class )
public class DefaultConfigurationHelper
    extends AbstractLogEnabled
    implements ConfigurationHelper
{
    @Requirement
    private PasswordHelper passwordHelper;
    
    private static final String PASSWORD_MASK = "*****";
    
    /**
     * XStream is used for a deep clone (TODO: not sure if this is a great idea)
     */
    private static XStream xstream = new XStream();
    
    public Configuration clone( Configuration config )
    {
        // use Xstream
        return (Configuration) xstream.fromXML( xstream.toXML( config ));
    }
    
    public void encryptDecryptPasswords( Configuration config, boolean encrypt )
    {
        handlePasswords( config, encrypt, false );        
    }

    public void maskPasswords( Configuration config )
    {
        handlePasswords( config, false, true );
    }
    
    private void handlePasswords( Configuration config, boolean encrypt, boolean mask)
    {
        if ( config.getErrorReporting() != null 
            && StringUtils.isNotEmpty( config.getErrorReporting().getJiraPassword() ) )
        {
            CErrorReporting errorConfig = config.getErrorReporting();
            errorConfig.setJiraPassword( encryptDecryptPassword( errorConfig.getJiraPassword(), encrypt, mask ) );
        }
        
        if ( config.getSmtpConfiguration() != null
            && StringUtils.isNotEmpty( config.getSmtpConfiguration().getPassword() ) )
        {
            CSmtpConfiguration smtpConfig = config.getSmtpConfiguration();
            smtpConfig.setPassword( encryptDecryptPassword( smtpConfig.getPassword(), encrypt, mask ) );
        }
        
        // global proxy
        if ( config.getGlobalHttpProxySettings() != null &&
            config.getGlobalHttpProxySettings().getAuthentication() != null &&
            StringUtils.isNotEmpty( config.getGlobalHttpProxySettings().getAuthentication().getPassword() ) )
        {
            CRemoteAuthentication auth = config.getGlobalHttpProxySettings().getAuthentication();
            auth.setPassword( encryptDecryptPassword( auth.getPassword(), encrypt, mask ) );
        }
        
        // each repo
        for ( CRepository repo : (List<CRepository>)config.getRepositories() )
        {   
            // remote auth
            if( repo.getRemoteStorage() != null && 
                repo.getRemoteStorage().getAuthentication() != null && 
                StringUtils.isNotEmpty( repo.getRemoteStorage().getAuthentication().getPassword() ) )
            {
                CRemoteAuthentication auth = repo.getRemoteStorage().getAuthentication();
                auth.setPassword( encryptDecryptPassword( auth.getPassword(), encrypt, mask ) );
            }
            
            // proxy auth
            if( repo.getRemoteStorage() != null && 
                repo.getRemoteStorage().getHttpProxySettings() != null &&
                repo.getRemoteStorage().getHttpProxySettings().getAuthentication() != null && 
                StringUtils.isNotEmpty( repo.getRemoteStorage().getHttpProxySettings().getAuthentication().getPassword() ) )
            {
                CRemoteAuthentication auth = repo.getRemoteStorage().getHttpProxySettings().getAuthentication();
                auth.setPassword( encryptDecryptPassword( auth.getPassword(), encrypt, mask ) );
            }
        }
    }
    
    private String encryptDecryptPassword( String password, boolean encrypt, boolean mask )
    {
        if ( mask )
        {
            return PASSWORD_MASK;
        }
        
        if ( encrypt )
        {
            try
            {
                return passwordHelper.encrypt( password );
            }
            catch ( PlexusCipherException e )
            {
                getLogger().error( "Failed to encrypt password in nexus.xml.", e );
            }
        }
        else
        {
            try
            {
                return passwordHelper.decrypt( password );
            }
            catch ( PlexusCipherException e )
            {
                getLogger().error( "Failed to decrypt password in nexus.xml.", e );
            }
        }

        return password;
    }
}
