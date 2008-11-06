package org.apache.maven.mercury.repository.metadata;

import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * adds new plugin to metadata
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class AddPluginOperation
    implements MetadataOperation
{
    private static final Language lang = new DefaultLanguage( AddPluginOperation.class );

    private Plugin plugin;

    /**
     * @throws MetadataException
     */
    public AddPluginOperation( PluginOperand data )
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
     * add version to the in-memory metadata instance
     * 
     * @param metadata
     * @param version
     * @return
     * @throws MetadataException
     */
    public boolean perform( Metadata metadata )
        throws MetadataException
    {
        if ( metadata == null )
            return false;

        List<Plugin> plugins = metadata.getPlugins();

        for ( Iterator<Plugin> pi = plugins.iterator(); pi.hasNext(); )
        {
            Plugin p = pi.next();

            if ( p.getArtifactId().equals( plugin.getArtifactId() ) )
            {
                // plugin already enlisted
                return false;
            }
        }

        // not found, add it
        plugins.add( plugin );

        return true;
    }

}
