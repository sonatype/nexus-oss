package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.ExternalConfiguration;
import org.sonatype.nexus.util.Inflector;

/**
 * A helper class that holds an Xpp3Dom and maintains it.
 * 
 * @author cstamas
 */
public class AbstractRepositoryConfiguration
    implements ExternalConfiguration
{
    private Xpp3Dom configuration;

    private Xpp3Dom changedConfiguration;

    public AbstractRepositoryConfiguration( Xpp3Dom configuration )
    {
        this.configuration = configuration;
    }

    public boolean isDirty()
    {
        return this.changedConfiguration != null;
    }

    public void commitChanges()
    {
        if ( changedConfiguration != null )
        {
            Xpp3Dom result = Xpp3Dom.mergeXpp3Dom( changedConfiguration, configuration );

            // shave off config root node
            while ( configuration.getChildCount() > 0 )
            {
                configuration.removeChild( 0 );
            }

            // and put beneath it the merge result
            for ( int i = 0; i < result.getChildCount(); i++ )
            {
                configuration.addChild( result.getChild( i ) );
            }

            changedConfiguration = null;
        }
    }

    public void rollbackChanges()
    {
        changedConfiguration = null;
    }

    public Object getConfiguration()
    {
        return configuration;
    }

    public Xpp3Dom getConfiguration( boolean forModification )
    {
        if ( forModification )
        {
            // copy configuration if needed
            if ( changedConfiguration == null )
            {
                changedConfiguration = copyTree( configuration );
            }

            return changedConfiguration;
        }
        else
        {
            return configuration;
        }
    }

    protected Xpp3Dom copyTree( Xpp3Dom root )
    {
        Xpp3Dom clone = new Xpp3Dom( root.getName() );

        // copy attributes
        for ( String attrName : root.getAttributeNames() )
        {
            clone.setAttribute( attrName, root.getAttribute( attrName ) );
        }

        // copy value
        clone.setValue( root.getValue() );

        // copy children
        for ( Xpp3Dom rootChild : root.getChildren() )
        {
            Xpp3Dom cloneChild = copyTree( rootChild );

            clone.addChild( cloneChild );
        }

        return clone;
    }

    /**
     * Gets the node value, creating it on the fly if not existing.
     * 
     * @param parent
     * @param name
     * @param defaultValue
     * @return
     */
    protected String getNodeValue( Xpp3Dom parent, String name, String defaultValue )
    {
        Xpp3Dom node = parent.getChild( name );

        if ( node == null )
        {
            return defaultValue;
            
            // do NOT create nodes for inspection, only explicitly with setNodeValue()
            // node = new Xpp3Dom( name );

            // node.setValue( defaultValue );

            // parent.addChild( node );
        }

        return node.getValue();
    }

    /**
     * Sets node value, creating it on the fly if not existing.
     * 
     * @param parent
     * @param name
     * @param value
     */
    protected void setNodeValue( Xpp3Dom parent, String name, String value )
    {
        Xpp3Dom node = parent.getChild( name );

        if ( node == null )
        {
            node = new Xpp3Dom( name );

            parent.addChild( node );
        }

        node.setValue( value );
    }

    /**
     * Gets a collection as unmodifiable list.
     * 
     * @param parent
     * @param name
     * @return
     */
    protected List<String> getCollection( Xpp3Dom parent, String name )
    {
        Xpp3Dom node = parent.getChild( name );

        if ( node == null )
        {
            return Collections.emptyList();
            
            // do NOT create nodes for inspection, only explicitly with setNodeValue()
            // node = new Xpp3Dom( name );

            // parent.addChild( node );
        }

        ArrayList<String> result = new ArrayList<String>( node.getChildCount() );

        for ( Xpp3Dom child : node.getChildren() )
        {
            result.add( child.getValue() );
        }

        return Collections.unmodifiableList( result );
    }

    protected void setCollection( Xpp3Dom parent, String name, Collection<String> values )
    {
        Xpp3Dom node = parent.getChild( name );

        if ( node != null )
        {
            for ( int i = 0; i < parent.getChildCount(); i++ )
            {
                Xpp3Dom existing = parent.getChild( i );

                if ( StringUtils.equals( name, existing.getName() ) )
                {
                    parent.removeChild( i );

                    break;
                }
            }

            node = null;
        }

        node = new Xpp3Dom( name );

        parent.addChild( node );

        String childName = Inflector.getInstance().singularize( name );

        for ( String childVal : values )
        {
            Xpp3Dom child = new Xpp3Dom( childName );

            child.setValue( childVal );

            node.addChild( child );
        }
    }

    /**
     * Adds a value to collection. Collection node is created on the fly.
     * 
     * @param parent
     * @param name
     * @param value
     * @return
     */
    protected boolean addToCollection( Xpp3Dom parent, String name, String value, boolean keepUnique )
    {
        Xpp3Dom node = parent.getChild( name );

        if ( node == null )
        {
            node = new Xpp3Dom( name );

            parent.addChild( node );
        }

        // if keeping it unique, we must first look is value aready in
        if ( keepUnique )
        {
            for ( Xpp3Dom child : node.getChildren() )
            {
                if ( StringUtils.equals( value, child.getValue() ) )
                {
                    return false;
                }
            }
        }

        String childName = Inflector.getInstance().singularize( name );

        Xpp3Dom child = new Xpp3Dom( childName );

        child.setValue( value );

        node.addChild( child );

        return true;
    }

    /**
     * Removes a value from a collection. Collection node is created if not exists.
     * 
     * @param parent
     * @param name
     * @param value
     */
    protected boolean removeFromCollection( Xpp3Dom parent, String name, String value )
    {
        Xpp3Dom node = parent.getChild( name );

        if ( node == null )
        {
            // do NOT create nodes for inspection, only explicitly with setNodeValue()
            return false;
            
            // node = new Xpp3Dom( name );

            // parent.addChild( node );
        }

        for ( int i = 0; i < node.getChildCount(); i++ )
        {
            Xpp3Dom child = node.getChild( i );

            if ( StringUtils.equals( value, child.getValue() ) )
            {
                node.removeChild( i );

                return true;
            }
        }

        return false;
    }

}
