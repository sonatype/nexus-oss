package org.sonatype.nexus.proxy.maven.wl.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.proxy.maven.wl.EntrySource;
import org.sonatype.nexus.proxy.maven.wl.WritableEntrySource;

public class WritableEntrySourceModifierImplTest
{
    protected final String[] entries1 = new String[] { "/org/sonatype", "/org/apache" };

    protected final String[] entries2 = new String[] { "/org/sonatype/nexus", "/org/apache/maven" };

    private WritableEntrySource writableEntrySource;

    private WritableEntrySourceModifierImpl wesm;

    @Before
    public void prepare()
        throws IOException
    {
        writableEntrySource = new WritableArrayListEntrySource( Arrays.asList( entries1 ) );
        wesm = new WritableEntrySourceModifierImpl( writableEntrySource );
    }

    @Test
    public void smoke()
        throws IOException
    {
        assertThat( "No changes added yet", !wesm.hasChanges() );
        assertThat( "No changes added yet", !wesm.apply() );
        assertThat( "No changes added yet", !wesm.reset() );

    }

    @Test
    public void offeringPathsNotModifyingWL()
    {
        // offering paths that would not modify WL
        assertThat( "WL would not be changed", !wesm.offerEntries( "/org/sonatype/nexus" ) );
        assertThat( "No changes added yet", !wesm.hasChanges() );
        assertThat( "WL would not be changed", !wesm.offerEntries( "/org/apache/maven" ) );
        assertThat( "No changes added yet", !wesm.hasChanges() );
    }

    @Test
    public void offeringPathsModifyingWLAndReset()
        throws IOException
    {
        // offering paths that modify WL and reset
        assertThat( "WL is changed", wesm.offerEntries( "/com/sonatype/nexus" ) );
        assertThat( "Changes were added", wesm.hasChanges() );
        assertThat( "WL is changed", wesm.offerEntries( "/com/mycorp" ) );
        assertThat( "Changes were added", wesm.hasChanges() );
        assertThat( "Changes were added", wesm.reset() );
        assertThat( "No changes added yet, wesm was reset", !wesm.hasChanges() );
        assertThat( "Entries unchanged", writableEntrySource.readEntries(), hasItems( entries1 ) );
    }

    @Test
    public void offeringPathsModifyingWLAndApply()
        throws IOException
    {
        // offering paths that modify WL and apply
        assertThat( "WL is changed", wesm.offerEntries( "/com/sonatype/nexus" ) );
        assertThat( "Changes were added", wesm.hasChanges() );
        assertThat( "WL is changed", wesm.offerEntries( "/com/mycorp" ) );
        assertThat( "Changes were added", wesm.hasChanges() );
        assertThat( "Changes were added", wesm.apply() );
        assertThat( "No changes added yet, wesm was applied", !wesm.hasChanges() );
        assertThat( "Entries unchanged", writableEntrySource.readEntries(), hasItems( entries1 ) );
        assertThat( "Entries unchanged", writableEntrySource.readEntries(), hasItems( new String[] { "/com/sonatype/nexus",
            "/com/mycorp" } ) );
    }

    @Test
    public void revokingPathsNotModifyingWL()
    {
        // revoking paths that would not modify WL
        assertThat( "WL would not be changed", !wesm.revokeEntries( "/com" ) ); // not in WL
        assertThat( "No changes added yet", !wesm.hasChanges() );
        assertThat( "WL would not be changed", !wesm.revokeEntries( "/com/sonatype" ) ); // not in WL
        assertThat( "No changes added yet", !wesm.hasChanges() );
        assertThat( "WL would not be changed", !wesm.revokeEntries( "/org/sonatype/nexus" ) ); // parent is in list
        assertThat( "No changes added yet", !wesm.hasChanges() );
        assertThat( "WL would not be changed", !wesm.revokeEntries( "/org/apache/maven" ) ); // parent is in list
        assertThat( "No changes added yet", !wesm.hasChanges() );
    }

    @Test
    public void revokingPathsModifyingWLAndReset()
        throws IOException
    {
        // revoking paths that modify WL and reset
        assertThat( "WL is changed", wesm.revokeEntries( "/org/sonatype" ) );
        assertThat( "Changes were added", wesm.hasChanges() );
        assertThat( "WL is changed", wesm.revokeEntries( "/org/apache" ) );
        assertThat( "Changes were added", wesm.hasChanges() );
        assertThat( "WL would not be changed", !wesm.revokeEntries( "/com/sonatype" ) ); // not in WL
        assertThat( "No changes added yet", wesm.hasChanges() );
        assertThat( "Changes were added", wesm.reset() );
        assertThat( "No changes added yet, wesm was reset", !wesm.hasChanges() );
        assertThat( "Entries unchanged", writableEntrySource.readEntries(), hasItems( entries1 ) );
    }

    @Test
    public void revokingPathsModifyingWLAndApply()
        throws IOException
    {
        // revoking paths that modify WL and apply
        assertThat( "WL is changed", wesm.revokeEntries( "/org/sonatype" ) );
        assertThat( "Changes were added", wesm.hasChanges() );
        assertThat( "WL is changed", wesm.revokeEntries( "/org/apache" ) );
        assertThat( "Changes were added", wesm.hasChanges() );
        assertThat( "WL would not be changed", !wesm.revokeEntries( "/com/sonatype" ) ); // not in WL
        assertThat( "No changes added yet", wesm.hasChanges() );
        assertThat( "Changes were added", wesm.apply() );
        assertThat( "No changes added yet, wesm was applied", !wesm.hasChanges() );
        assertThat( "Entries removed", writableEntrySource.readEntries(), not( hasItems( entries1 ) ) );
        assertThat( "Entries removed", writableEntrySource.readEntries().size(), is( 0 ) );
    }

    @Test
    public void modifyingFreely()
        throws IOException
    {
        // Note: using entries2 that has 3 depth entries
        writableEntrySource = new WritableArrayListEntrySource( Arrays.asList( entries2 ) );
        wesm = new WritableEntrySourceModifierImpl( writableEntrySource );

        assertThat( "WL would not be changed", !wesm.revokeEntries( "/org/sonatype" ) ); // child is in list
        assertThat( "No changes added yet", !wesm.hasChanges() );
        assertThat( "WL would not be changed", !wesm.revokeEntries( "/org/apache" ) ); // child is in list
        assertThat( "No changes added yet", !wesm.hasChanges() );
        assertThat( "WL would not be changed", wesm.revokeEntries( "/org/sonatype/nexus" ) );
        assertThat( "No changes added yet", wesm.hasChanges() );
        assertThat( "WL would not be changed", wesm.revokeEntries( "/org/apache/maven" ) );
        assertThat( "No changes added yet", wesm.hasChanges() );

        // adding some
        assertThat( "WL is changed", wesm.offerEntries( "/com/sonatype/nexus" ) );
        assertThat( "Changes were added", wesm.hasChanges() );
        assertThat( "WL is changed", wesm.offerEntries( "/com/mycorp" ) );
        assertThat( "Changes were added", wesm.hasChanges() );

        assertThat( "Changes were added", wesm.apply() );

        assertThat( writableEntrySource.readEntries(), contains( "/com/sonatype/nexus", "/com/mycorp" ) );
    }

    // ==

    @Test
    public void edgeCase1()
        throws IOException
    {
        writableEntrySource = new WritableArrayListEntrySource( Arrays.asList( entries1 ) );
        wesm = new WritableEntrySourceModifierImpl( writableEntrySource );

        assertThat( "WL would not be changed", !wesm.revokeEntries( "/org/sonatype/nexus" ) ); // parent is in list
        assertThat( "No changes added yet", !wesm.hasChanges() );
        assertThat( "WL would not be changed", !wesm.revokeEntries( "/org/apache/maven" ) ); // parent is in list
        assertThat( "No changes added yet", !wesm.hasChanges() );
        assertThat( "WL would not be changed", wesm.revokeEntries( "/org/sonatype" ) );
        assertThat( "No changes added yet", wesm.hasChanges() );
        assertThat( "WL would not be changed", wesm.revokeEntries( "/org/apache" ) );
        assertThat( "No changes added yet", wesm.hasChanges() );

        // adding some
        assertThat( "WL is changed", wesm.offerEntries( "/com/sonatype/nexus" ) );
        assertThat( "Changes were added", wesm.hasChanges() );
        assertThat( "WL is changed", wesm.offerEntries( "/com/mycorp" ) );
        assertThat( "Changes were added", wesm.hasChanges() );

        assertThat( "Changes were added", wesm.apply() );

        assertThat( writableEntrySource.readEntries(), contains( "/com/sonatype/nexus", "/com/mycorp" ) );
    }

    @Test
    public void edgeCase2()
        throws IOException
    {
        writableEntrySource = new WritableArrayListEntrySource( Arrays.asList( entries2 ) );
        wesm = new WritableEntrySourceModifierImpl( writableEntrySource );

        assertThat( "WL would not be changed", !wesm.revokeEntries( "/org/sonatype" ) ); // child is in list
        assertThat( "No changes added yet", !wesm.hasChanges() );
        assertThat( "WL would not be changed", !wesm.revokeEntries( "/org/apache" ) ); // child is in list
        assertThat( "No changes added yet", !wesm.hasChanges() );
        assertThat( "WL would not be changed", wesm.revokeEntries( "/org/sonatype/nexus" ) );
        assertThat( "No changes added yet", wesm.hasChanges() );
        assertThat( "WL would not be changed", wesm.revokeEntries( "/org/apache/maven" ) );
        assertThat( "No changes added yet", wesm.hasChanges() );

        // adding some
        assertThat( "WL is changed", wesm.offerEntries( "/com/sonatype/nexus" ) );
        assertThat( "Changes were added", wesm.hasChanges() );
        assertThat( "WL is changed", wesm.offerEntries( "/com/mycorp" ) );
        assertThat( "Changes were added", wesm.hasChanges() );

        assertThat( "Changes were added", wesm.apply() );

        assertThat( writableEntrySource.readEntries(), contains( "/com/sonatype/nexus", "/com/mycorp" ) );
    }

    // ==

    public static class WritableArrayListEntrySource
        implements WritableEntrySource
    {
        private List<String> entries;

        private long created;

        /**
         * Constructor with entries. Will have last modified timestamp as "now" (moment of creation).
         * 
         * @param entries list of entries, might not be {@code null}
         */
        public WritableArrayListEntrySource( final List<String> entries )
        {
            this( entries, System.currentTimeMillis() );
        }

        /**
         * Constructor with entries and timestamp.
         * 
         * @param entries list of entries, might not be {@code null}.
         * @param created the timestamp this instance should report.
         */
        public WritableArrayListEntrySource( final List<String> entries, final long created )
        {
            this.entries = entries;
            this.created = entries != null ? created : -1;
        }

        @Override
        public boolean exists()
        {
            return entries != null;
        }

        @Override
        public List<String> readEntries()
            throws IOException
        {
            return entries;
        }

        @Override
        public long getLostModifiedTimestamp()
        {
            return created;
        }

        @Override
        public void writeEntries( EntrySource entrySource )
            throws IOException
        {
            this.entries = entrySource.readEntries();
            this.created = System.currentTimeMillis();
        }

        @Override
        public void delete()
            throws IOException
        {
            entries = null;
            created = -1;
        }
    }
}
