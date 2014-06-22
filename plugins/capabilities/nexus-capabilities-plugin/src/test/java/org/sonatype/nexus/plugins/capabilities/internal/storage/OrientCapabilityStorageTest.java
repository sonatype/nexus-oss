package org.sonatype.nexus.plugins.capabilities.internal.storage;

import java.io.File;
import java.util.Map;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugins.capabilities.internal.storage.OrientCapabilityStorage.DB_LOCATION;

/**
 * Tests for {@link OrientCapabilityStorage}.
 */
public class OrientCapabilityStorageTest
  extends TestSupport
{
  private OrientCapabilityStorage underTest;

  @Before
  public void setUp() throws Exception {
    ApplicationDirectories applicationDirectories = mock(ApplicationDirectories.class);
    File dir = util.createTempDir("testdb");
    when(applicationDirectories.getWorkDirectory(DB_LOCATION)).thenReturn(dir);

    this.underTest = new OrientCapabilityStorage(applicationDirectories);
    underTest.start();
  }

  @After
  public void tearDown() throws Exception {
    if (underTest != null) {
      underTest.stop();
    }
  }

  @Test
  public void getAll_empty() throws Exception {
    Map<CapabilityIdentity, CapabilityStorageItem> items = underTest.getAll();
    assertThat(items, notNullValue());
    assertThat(items.isEmpty(), is(true));
  }

  @Test
  public void basicLifecycle() throws Exception {
    CapabilityStorageItem item1 = new CapabilityStorageItem();
    item1.setType("test");
    item1.setVersion(0);
    item1.setEnabled(false);
    item1.setNotes("hello");
    Map<String,String> props = Maps.newHashMap();
    props.put("foo", "bar");
    item1.setProperties(props);
    log("Item: {}", item1);

    // add
    CapabilityIdentity id = underTest.add(item1);
    log("Id: {}", id);

    // list
    Map<CapabilityIdentity, CapabilityStorageItem> items1 = underTest.getAll();
    log("Items: {}", items1);
    assertThat(items1, notNullValue());
    assertThat(items1.entrySet(), hasSize(1));

    // verify that item has same attributes as original
    CapabilityStorageItem item2 = items1.values().iterator().next();
    assertThat(item2.getType(), is(item1.getType()));
    assertThat(item2.getVersion(), is(item1.getVersion()));
    assertThat(item2.getNotes(), is(item1.getNotes()));
    assertThat(item2.isEnabled(), is(item1.isEnabled()));
    assertThat(item2.getProperties(), hasEntry("foo", "bar"));

    // update
    item2.setEnabled(true);
    boolean updated = underTest.update(id, item2);
    assertThat("failed to update", updated, is(true));

    // verify that all is the same, except for enabled flag
    CapabilityStorageItem item3 = underTest.getAll().values().iterator().next();
    assertThat(item3, notNullValue());
    assertThat(item3.getType(), is(item1.getType()));
    assertThat(item3.getVersion(), is(item1.getVersion()));
    assertThat(item3.getNotes(), is(item1.getNotes()));
    assertThat(item3.isEnabled(), is(true));
    assertThat(item3.getProperties(), hasEntry("foo", "bar"));

    // delete
    boolean removed = underTest.remove(id);
    assertThat("failed to remove", removed, is(true));

    Map<CapabilityIdentity, CapabilityStorageItem> items2 = underTest.getAll();
    log("Items: {}", items2);
    assertThat(items2, notNullValue());
    assertThat(items2.isEmpty(), is(true));
  }
}
