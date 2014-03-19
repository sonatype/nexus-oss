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

package org.sonatype.nexus.quartz.internal.store;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.quartz.internal.store.TriggerWrapper.State;
import org.sonatype.nexus.util.NexusUberClassloader;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.kazuki.v0.store.KazukiException;
import io.kazuki.v0.store.Key;
import io.kazuki.v0.store.keyvalue.KeyValueIterable;
import io.kazuki.v0.store.keyvalue.KeyValuePair;
import io.kazuki.v0.store.keyvalue.KeyValueStore;
import io.kazuki.v0.store.keyvalue.KeyValueStoreIteration.SortDirection;
import io.kazuki.v0.store.lifecycle.Lifecycle;
import io.kazuki.v0.store.schema.SchemaStore;
import org.quartz.Calendar;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.spi.OperableTrigger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Kazuki backed DAO implementation. This class merely handles CRUD operations, and transformation of the persisted
 * data into entities.
 *
 * @since 2.8
 */
@Singleton
@Named
public class KazukiJobDao
    extends LifecycleSupport
{
  private final KeyValueStore store;

  private final SchemaStore schemaStore;

  private final Lifecycle lifecycle;

  private final NexusUberClassloader nexusUberClassloader;

  private final ObjectMapper mapper;

  @Inject
  public KazukiJobDao(final @Named("nexusquartz") KeyValueStore store,
                      final @Named("nexusquartz") SchemaStore schemaStore,
                      final @Named("nexusquartz") Lifecycle lifecycle,
                      final NexusUberClassloader nexusUberClassloader)
      throws Exception
  {
    this.store = checkNotNull(store);
    this.schemaStore = checkNotNull(schemaStore);
    this.lifecycle = checkNotNull(lifecycle);
    this.nexusUberClassloader = checkNotNull(nexusUberClassloader);
    this.mapper = createJackson();
  }

  private ObjectMapper createJackson() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    mapper.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.SETTER, Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.CREATOR, Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE);
    return mapper;
  }

  @Override
  protected void doStart() throws Exception {
    lifecycle.init();
    lifecycle.start();

    JobDetailRecord.SCHEMA.mayCreateSchema(schemaStore);
    TriggerRecord.SCHEMA.mayCreateSchema(schemaStore);
    CalendarRecord.SCHEMA.mayCreateSchema(schemaStore);
  }

  @Override
  protected void doStop() throws Exception {
    lifecycle.stop();
    lifecycle.shutdown();
  }

  // == JobDao API

  public void nukeStore() throws KazukiException {
    store.clear(JobDetailRecord.SCHEMA.getName());
    store.clear(TriggerRecord.SCHEMA.getName());
    store.clear(CalendarRecord.SCHEMA.getName());
  }

  // ==

  public KeyValuePair<JobKey> createJobDetail(final JobDetail value) throws KazukiException {
    final JobDetailRecord record = convert(value);
    final Key key = create(JobDetailRecord.SCHEMA, record);
    return new KeyValuePair(key, value.getKey());
  }

  public List<KeyValuePair<JobDetail>> readJobDetails(final Predicate<JobDetailRecord> predicate)
      throws KazukiException
  {
    return readJobDetails(predicate, toJobDetail());
  }

  public <T> List<KeyValuePair<T>> readJobDetails(final Predicate<JobDetailRecord> predicate,
                                                  final Function<JobDetailRecord, T> function)
      throws KazukiException
  {
    checkNotNull(function);
    final List<KeyValuePair<JobDetailRecord>> kvs = locateRecords(JobDetailRecord.SCHEMA, predicate);
    final List<KeyValuePair<T>> result = Lists.newArrayListWithCapacity(kvs.size());
    for (KeyValuePair<JobDetailRecord> kv : kvs) {
      result.add(new KeyValuePair(kv.getKey(), function.apply(kv.getValue())));
    }
    return result;
  }

  public boolean updateJobDetail(final Key key, final JobDetail value) throws KazukiException {
    return update(key, JobDetailRecord.SCHEMA, convert(value));
  }

  public boolean deleteJobDetail(final Key key) throws KazukiException {
    return delete(key, JobDetailRecord.SCHEMA);
  }

  // ==

  public KeyValuePair<TriggerWrapper> createOperableTrigger(final TriggerWrapper value)
      throws KazukiException
  {
    final TriggerRecord record = convert(value);
    final Key key = create(TriggerRecord.SCHEMA, record);
    return new KeyValuePair(key, value);
  }

  public KeyValuePair<TriggerWrapper> readOperableTrigger(final Key key)
      throws KazukiException
  {
    final TriggerRecord record = read(key, TriggerRecord.SCHEMA);
    if (record != null) {
      return new KeyValuePair<TriggerWrapper>(key, convert(record));
    }
    else {
      return null;
    }
  }

  public List<KeyValuePair<TriggerWrapper>> readOperableTriggers(final Predicate<TriggerRecord> predicate)
      throws KazukiException
  {
    return readOperableTriggers(predicate, toOperableTriger());
  }

  public <T> List<KeyValuePair<T>> readOperableTriggers(final Predicate<TriggerRecord> predicate,
                                                        final Function<TriggerRecord, T> function)
      throws KazukiException
  {
    checkNotNull(function);
    final List<KeyValuePair<TriggerRecord>> kvs = locateRecords(TriggerRecord.SCHEMA, predicate);
    final List<KeyValuePair<T>> result = Lists.newArrayListWithCapacity(kvs.size());
    for (KeyValuePair<TriggerRecord> kv : kvs) {
      result.add(new KeyValuePair<>(kv.getKey(), function.apply(kv.getValue())));
    }
    return result;
  }

  public boolean updateOperableTrigger(final Key key, final TriggerWrapper value)
      throws KazukiException
  {
    return update(key, TriggerRecord.SCHEMA, convert(value));
  }

  public boolean deleteOperableTrigger(final Key key) throws KazukiException {
    return delete(key, TriggerRecord.SCHEMA);
  }

  // ==

  public KeyValuePair<String> createCalendar(final String name, final Calendar value) throws KazukiException {
    final CalendarRecord record = convert(name, value);
    final Key key = create(CalendarRecord.SCHEMA, record);
    return new KeyValuePair(key, name);
  }

  public List<KeyValuePair<Calendar>> readCalendars(final Predicate<CalendarRecord> predicate) throws KazukiException {
    return readCalendars(predicate, toCalendar());
  }

  public <T> List<KeyValuePair<T>> readCalendars(final Predicate<CalendarRecord> predicate,
                                                 final Function<CalendarRecord, T> function) throws KazukiException
  {
    checkNotNull(function);
    final List<KeyValuePair<CalendarRecord>> kvs = locateRecords(CalendarRecord.SCHEMA, predicate);
    final List<KeyValuePair<T>> result = Lists.newArrayListWithCapacity(kvs.size());
    for (KeyValuePair<CalendarRecord> kv : kvs) {
      result.add(new KeyValuePair(kv.getKey(), function.apply(kv.getValue())));
    }
    return result;
  }

  public boolean updateCalendar(final Key key, final String name, final Calendar value) throws KazukiException {
    return update(key, CalendarRecord.SCHEMA, convert(name, value));
  }

  public boolean deleteCalendar(final Key key) throws KazukiException {
    return delete(key, CalendarRecord.SCHEMA);
  }

  // ==

  public Function<JobDetailRecord, JobDetail> toJobDetail() {
    return new Function<JobDetailRecord, JobDetail>()
    {
      @Override
      public JobDetail apply(final JobDetailRecord input) {
        return convert(input);
      }
    };
  }

  public Function<TriggerRecord, TriggerWrapper> toOperableTriger() {
    return new Function<TriggerRecord, TriggerWrapper>()
    {
      @Override
      public TriggerWrapper apply(final TriggerRecord input) {
        return convert(input);
      }
    };
  }

  public Function<TriggerRecord, State> toOperableTrigerState() {
    return new Function<TriggerRecord, State>()
    {
      @Override
      public State apply(final TriggerRecord input) {
        return input.getState();
      }
    };
  }

  public Function<CalendarRecord, Calendar> toCalendar() {
    return new Function<CalendarRecord, Calendar>()
    {
      @Override
      public Calendar apply(final CalendarRecord input) {
        return convert(input);
      }
    };
  }

  public Function<CalendarRecord, String> toCalendarName() {
    return new Function<CalendarRecord, String>()
    {
      @Override
      public String apply(final CalendarRecord input) {
        return input.getName();
      }
    };
  }

  // == Internals

  @VisibleForTesting
  public <T> List<KeyValuePair<T>> locateRecords(final TypedSchema<T> type,
                                                 final Predicate<T> filter)
  {
    checkNotNull(type);
    final List<KeyValuePair<T>> result = Lists.newArrayList();
    try (final KeyValueIterable<KeyValuePair<T>> kvi = store.iterators()
        .entries(type.getName(), type.getClazz(), SortDirection.ASCENDING)) {
      for (KeyValuePair<T> kv : kvi) {
        final T record = kv.getValue();
        if (filter != null && !filter.apply(record)) {
          continue;
        }
        result.add(kv);
      }
    }
    return result;
  }

  // ==

  private <T> Key create(TypedSchema<T> schema, T value) throws KazukiException {
    return store.create(schema.getName(), schema.getClazz(), value, schema.getTypeValidation());
  }

  private <T> T read(Key key, TypedSchema<T> schema) throws KazukiException {
    return store.retrieve(key, schema.getClazz());
  }

  private <T> boolean update(Key key, TypedSchema<T> schema, T value) throws KazukiException {
    return store.update(key, schema.getClazz(), value);
  }

  private boolean delete(Key key, TypedSchema<?> schema) throws KazukiException {
    if (!schema.getName().equals(key.getTypePart())) {
      throw new KazukiException("Key " + key + " does not correspond to schema " + schema.getName());
    }
    return store.delete(key);
  }

  // ==

  private JobDetailRecord convert(final JobDetail jobDetail) {
    try {
      return new JobDetailRecord(
          jobDetail.getKey().getName(),
          jobDetail.getKey().getGroup(),
          jobDetail.getClass().getName(),
          jobDetail.getJobClass().getName(),
          mapper.<Map<String, Object>>convertValue(jobDetail, new TypeReference<Map<String, Object>>() {})
      );
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  private TriggerRecord convert(final TriggerWrapper wrapper) {
    try {
      String jobName = null;
      String jobGroup = null;
      if (wrapper.getOperableTrigger().getJobKey() != null) {
        jobName = wrapper.getOperableTrigger().getJobKey().getName();
        jobGroup = wrapper.getOperableTrigger().getJobKey().getName();
      }
      return new TriggerRecord(
          wrapper.getOperableTrigger().getKey().getName(),
          wrapper.getOperableTrigger().getKey().getGroup(),
          wrapper.getOperableTrigger().getClass().getName(),
          mapper.<Map<String, Object>>convertValue(wrapper.getOperableTrigger(),
              new TypeReference<Map<String, Object>>() {}),
          wrapper.getState(),
          jobName,
          jobGroup,
          wrapper.getOperableTrigger().getCalendarName()
      );
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  private CalendarRecord convert(final String calName, final Calendar calendar) {
    try {
      return new CalendarRecord(
          calName,
          calendar.getClass().getName(),
          mapper.<Map<String, Object>>convertValue(calendar, new TypeReference<Map<String, Object>>() {})
      );
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  private JobDetail convert(final JobDetailRecord record) {
    final ClassLoader original = Thread.currentThread().getContextClassLoader();
    try {
      final Class<?> clazz = Scheduler.class.getClassLoader().loadClass(record.getQuartzType());
      if (JobDetail.class.isAssignableFrom(clazz)) {
        Thread.currentThread().setContextClassLoader(nexusUberClassloader);
        return (JobDetail) mapper.convertValue(record.getData(), clazz);
      }
      else {
        throw new IllegalArgumentException("QuartzRecord type not compatible with JobDetail: " + record);
      }
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
    finally {
      Thread.currentThread().setContextClassLoader(original);
    }
  }

  private TriggerWrapper convert(final TriggerRecord record) {
    try {
      final Class<?> clazz = Scheduler.class.getClassLoader().loadClass(record.getQuartzType());
      if (OperableTrigger.class.isAssignableFrom(clazz)) {
        return new TriggerWrapper((OperableTrigger) mapper.convertValue(record.getData(), clazz), record.getState());
      }
      else {
        throw new IllegalArgumentException("QuartzRecord type not compatible with OperableTrigger: " + record);
      }
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  private Calendar convert(final CalendarRecord record) {
    try {
      final Class<?> clazz = Scheduler.class.getClassLoader().loadClass(record.getQuartzType());
      if (Calendar.class.isAssignableFrom(clazz)) {
        return (Calendar) mapper.convertValue(record.getData(), clazz);
      }
      else {
        throw new IllegalArgumentException("QuartzRecord type not compatible with Calendar: " + record);
      }
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }
}
