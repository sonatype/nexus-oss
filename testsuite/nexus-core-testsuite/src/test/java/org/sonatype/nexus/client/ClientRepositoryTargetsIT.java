/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Collection;

import org.junit.Test;
import org.sonatype.nexus.client.core.exception.NexusClientNotFoundException;
import org.sonatype.nexus.client.core.subsystem.targets.RepositoryTarget;
import org.sonatype.nexus.client.core.subsystem.targets.RepositoryTargets;

public class ClientRepositoryTargetsIT
    extends ClientITSupport
{

    public ClientRepositoryTargetsIT( final String nexusBundleCoordinates )
    {
        super( nexusBundleCoordinates );
    }

    @Test
    public void getTargets()
    {
        final Collection<RepositoryTarget> targets = targets().get();
        assertThat( targets, is( not( empty() ) ) );
    }

    @Test
    public void getTarget()
    {
        final RepositoryTarget target = targets().get().iterator().next();
        final RepositoryTarget direct = targets().get( target.id() );
        assertThat( direct.id(), is( target.id() ) );
        assertThat( direct.name(), is( target.name() ) );
        assertThat( direct.contentClass(), is( target.contentClass() ) );
    }

    @Test
    public void createTarget()
    {
        final String id = "created";
        createTarget( id, "test1", "test2" );

        final RepositoryTarget target = targets().get( id );
        assertThat( target.id(), is( id ) );
        assertThat( target.name(), is( id + "name" ) );
        assertThat( target.contentClass(), is( "maven2" ) );
        assertThat( target.patterns(), contains( "test1", "test2" ) );
    }

    private RepositoryTarget createTarget( final String id, final String... patterns )
    {
        return targets().create( id ).withName( id + "name" )
            .withContentClass( "maven2" ).withPatterns( patterns )
            .save();
    }

    @Test
    public void updateTarget()
    {
        RepositoryTarget target = createTarget( "updateTarget", "pattern1", "pattern2" );

        target.withName( "updatedTarget" ).addPattern( "pattern3" ).save();
        target = targets().get( "updateTarget" );
        assertThat( target.patterns(), hasItem( "pattern3" ) );
        assertThat( target.name(), is( "updatedTarget" ) );
    }

    @Test(expected = NexusClientNotFoundException.class )
    public void deleteTarget()
    {
        RepositoryTarget target = createTarget( "deleteTarget", "pattern1", "pattern2" ).remove();
        // targets.get(...) is expected to throw 404
        assertThat( targets().get( target.id() ), is( nullValue() ) );
    }

    @Test
    public void refreshTarget()
    {
        RepositoryTarget needsRefresh = createTarget( "deleteTarget", "pattern1", "pattern2" );
        targets().get( needsRefresh.id() ).withPatterns( "differentPattern" ).save();
        assertThat( needsRefresh.refresh().patterns(), contains( "differentPattern" ) );
    }

    private RepositoryTargets targets()
    {
        return client().getSubsystem( RepositoryTargets.class );
    }

}
