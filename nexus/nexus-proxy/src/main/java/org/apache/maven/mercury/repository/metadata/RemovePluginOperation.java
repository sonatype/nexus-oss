package org.apache.maven.mercury.repository.metadata;

import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * removes a Plugin from Metadata
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class RemovePluginOperation
    implements MetadataOperation
{
    private static final Language lang = new DefaultLanguage( RemovePluginOperation.class );

    private Plugin plugin;

    /**
     * @throws MetadataException
     */
    public RemovePluginOperation( PluginOperand data )
        throws MetadataException
    {
        setOperand( data );
    }

    public void setOperand( Object data )
        throws MetadataException
    {
        if ( data == null || !( data instanceof PluginOperand ) )
            throw new MetadataException( lang.getMessage( "bad.operand", "PluginOperand", data == null ? "null" : data
                .getClass().getName() ) );

        plugin = ( (PluginOperand) data ).getOperand();
    }

    /**
     * remove version to the in-memory metadata instance
     * 
     * @param metadata
     * @param version
     * @return
     */
    public boolean perform( Metadata metadata )
        throws MetadataException
    {
        if ( metadata == null )
            return false;

        List<Plugin> plugins = metadata.getPlugins();

        if ( plugins != null && plugins.size() > 0 )
        {
            for ( Iterator<Plugin> pi = plugins.iterator(); pi.hasNext(); )
            {
                Plugin p = pi.next();

                if ( p.getArtifactId().equals( plugin.getArtifactId() ) )
                {
                    pi.remove();

                    return true;
                }
            }
        }

        return false;
    }
}
