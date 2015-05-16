package org.sonatype.nexus.validation;

import java.lang.annotation.Annotation;

import javax.validation.ConstraintValidator;

import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * Support for {@link ConstraintValidator} implementations.
 *
 * @since 3.0
 */
public abstract class ConstraintValidatorSupport<A extends Annotation,T>
  extends ComponentSupport
  implements ConstraintValidator<A,T>
{
  @Override
  public void initialize(final A constraintAnnotation) {
    // empty
  }
}
