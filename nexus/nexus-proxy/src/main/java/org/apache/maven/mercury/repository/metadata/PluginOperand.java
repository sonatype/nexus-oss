package org.apache.maven.mercury.repository.metadata;

import org.codehaus.plexus.lang.DefaultLanguage;
import org.codehaus.plexus.lang.Language;

/**
 * Plugin storage
 * 
 * @author Oleg Gusakov
 * @version $Id$
 */
public class PluginOperand
    extends AbstractOperand
{
    private static final Language lang = new DefaultLanguage( PluginOperand.class );

    Plugin plugin;

    public PluginOperand( Plugin data )
    {
        this.plugin = data;
    }

    public Plugin getOperand()
    {
        return plugin;
    }
}
