package org.sonatype.nexus.repository.internal;

import javax.inject.Named;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.RepositoryImpl;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Repository module.
 */
@Named
public class RepositoryModule
    extends AbstractModule
{
  @Override
  protected void configure() {
    install(new FactoryModuleBuilder()
            .implement(Repository.class, RepositoryImpl.class)
            .build(RepositoryImpl.Factory.class)
    );
  }
}
