package de.is24.test.guice;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;


public abstract class GuiceTestRunner extends BlockJUnit4ClassRunner {
  private final Injector injector;

  public GuiceTestRunner(final Class<?> classToRun, Module... modules) throws InitializationError {
    super(classToRun);
    this.injector = Guice.createInjector(modules);
  }

  @Override
  public Object createTest() {
    return injector.getInstance(getTestClass().getJavaClass());
  }
}
