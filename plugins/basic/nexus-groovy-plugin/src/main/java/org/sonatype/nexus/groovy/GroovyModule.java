package org.sonatype.nexus.groovy;

import javax.inject.Named;
import javax.script.ScriptEngineFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;

/**
 * Groovy plugin Guice module.
 *
 * @since 2.7
 */
@Named
public class GroovyModule
  extends AbstractModule
{
  @Override
  protected void configure() {
    final GroovyScriptEngineFactory factory = new GroovyScriptEngineFactory();

    bind(GroovyScriptEngineFactory.class)
        .toInstance(factory);

    bind(ScriptEngineFactory.class)
        .annotatedWith(Names.named("groovy"))
        .toInstance(factory);
  }
}
