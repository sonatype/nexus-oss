package org.sonatype.nexus.proxy.attributes;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.uid.IsMetadataMaintainedAttribute;

import com.google.common.base.Preconditions;

public class DelegatingAttributeStorage
    extends AbstractAttributeStorage
    implements AttributeStorage
{
    private final AttributeStorage delegate;

    public DelegatingAttributeStorage( final AttributeStorage delegate )
    {
        this.delegate = Preconditions.checkNotNull( delegate );
    }

    public AttributeStorage getDelegate()
    {
        return delegate;
    }

    @Override
    public Attributes getAttributes( RepositoryItemUid uid )
    {
        if ( isMetadataMaintained( uid ) )
        {
            return delegate.getAttributes( uid );
        }

        return null;
    }

    @Override
    public void putAttributes( RepositoryItemUid uid, Attributes attributes )
    {
        if ( isMetadataMaintained( uid ) )
        {
            delegate.putAttributes( uid, attributes );
        }
    }

    @Override
    public boolean deleteAttributes( RepositoryItemUid uid )
    {
        if ( isMetadataMaintained( uid ) )
        {
            return delegate.deleteAttributes( uid );
        }
        
        return false;
    }

    // ==

    /**
     * Returns true if the attributes should be maintained at all.
     * 
     * @param uid
     * @return true if attributes should exists for given UID.
     */
    protected boolean isMetadataMaintained( final RepositoryItemUid uid )
    {
        Boolean isMetadataMaintained = uid.getAttributeValue( IsMetadataMaintainedAttribute.class );

        if ( isMetadataMaintained != null )
        {
            return isMetadataMaintained.booleanValue();
        }
        else
        {
            // safest
            return true;
        }
    }
}
