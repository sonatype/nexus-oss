package org.sonatype.nexus.plugins.ruby;

import java.io.InputStream;

import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.cuba.api.ApiV1DependenciesCuba;
import org.sonatype.nexus.ruby.cuba.quick.QuickMarshalCuba;

import org.slf4j.Logger;

public class PurgeBrokenFilesRubygemsWalkerProcessor
    extends AbstractWalkerProcessor
{

  private RubyRepository repository;

  private final Logger logger;

  private final RubygemsGateway rubygems;

  public PurgeBrokenFilesRubygemsWalkerProcessor(Logger logger, RubygemsGateway rubygems) {
    this.logger = logger;
    this.rubygems = rubygems;
  }

  @Override
  public void beforeWalk(WalkerContext context) throws Exception {
    repository = context.getRepository() instanceof RubyRepository ? (RubyRepository) context.getRepository() : null;
    setActive(repository != null);
  }

  @Override
  public void processItem(WalkerContext context, StorageItem item) {
    if (item instanceof StorageFileItem) {
      try {
        if (item.getName().endsWith(ApiV1DependenciesCuba.RUBY)) {
          try(InputStream is = ((StorageFileItem) item).getInputStream()) {
            rubygems.newDependencyHelper().add(is);
          }
          catch(Exception e){
            repository.deleteItem(true, new ResourceStoreRequest(item));
          }
        }
        else if (item.getName().endsWith(QuickMarshalCuba.GEMSPEC_RZ)) {
          try(InputStream is = ((StorageFileItem) item).getInputStream()) {
            rubygems.newGemspecHelper(is);
          }
          catch(Exception e){
            repository.deleteItem(true, new ResourceStoreRequest(item));
          }
        }
      }
      catch (Exception e) {
        logger.warn("Error occurred while processing item '" + item.getPath() + "'.", e);
      }
    }
  }
}