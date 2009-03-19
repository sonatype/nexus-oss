package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.application.ExternalConfiguration;
import org.sonatype.nexus.util.Inflector;

/**
 * A helper class that holds an Xpp3Dom and maintains it.
 * 
 * @author cstamas
 */
public abstract class AbstractRepositoryConfiguration
    implements ExternalConfiguration
{
    private Xpp3Dom configuration;

    private boolean dirty;

    public AbstractRepositoryConfiguration( Xpp3Dom configuration )
    {
        this.configuration = configuration;

        this.dirty = false;
    }

    public boolean isDirty()
    {
        return this.dirty;
    }

    public void unmarkDirty()
    {
        this.dirty = false;
    }

    protected Xpp3Dom getConfiguration()
    {
        return configuration;
    }

    protected void markDirty()
    {
        this.dirty = true;
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
            node = new Xpp3Dom( name );

            node.setValue( defaultValue );

            parent.addChild( node );

            markDirty();
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

        markDirty();
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
            node = new Xpp3Dom( name );

            parent.addChild( node );

            markDirty();
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

        markDirty();
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

        markDirty();

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
            node = new Xpp3Dom( name );

            parent.addChild( node );
        }

        for ( int i = 0; i < node.getChildCount(); i++ )
        {
            Xpp3Dom child = node.getChild( i );

            if ( StringUtils.equals( value, child.getValue() ) )
            {
                node.removeChild( i );

                markDirty();

                return true;
            }
        }

        return false;
    }

}
