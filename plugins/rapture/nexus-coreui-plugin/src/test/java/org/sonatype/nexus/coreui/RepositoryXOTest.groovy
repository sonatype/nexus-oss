package org.sonatype.nexus.coreui

import javax.validation.Validation
import javax.validation.Validator

import org.sonatype.nexus.validation.group.Create

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class RepositoryXOTest
    extends Specification
{

  @Shared
  Validator validator = Validation.buildDefaultValidatorFactory().validator

  static List INVALID_CHARACTERS = ['_', '#', ',', 'A', '*', ' ', '#', '\'', '\\', '/', '?', '<', '>', '|', ' ', '\r',
                                    '\n', '\t', ',', '.'].asImmutable()

  def "Name is always required"() {
    when:
      def validations = validator.validate(new RepositoryXO([attributes: [any: 'any']]))

    then:
      validations.size() == 1
      validations[0].propertyPath.toString() == 'name'
      validations[0].message == 'may not be empty'
  }

  def "Attributes are always required, and cannot be empty"() {
    when:
      def validations = validator.validate(new RepositoryXO([name: 'foo'] + init))

    then:
      validations.size() == 1
      validations[0].propertyPath.toString() == 'attributes'
      validations[0].message == 'may not be empty'

    where:
      init << [null, [:]].collect { [attributes: it] }
  }

  @Unroll
  def "The name: #init.name should not validate"() {
    setup:
      Map validConfiguration = [
          name      : 'foo',
          attributes: [any: 'any']
      ]

    when:
      def validations = validator.validate(new RepositoryXO(validConfiguration + init))

    then:
      validations.size() == 1
      validations[0].propertyPath.toString() == 'name'

    where:
      init << INVALID_CHARACTERS.collect { [name: it] }
  }

  @Unroll
  def "The name: #init.name should be valid"() {
    setup:
      Map validConfiguration = [
          name      : 'foo',
          attributes: [any: 'any']
      ]

    when:
      def validations = validator.validate(new RepositoryXO(validConfiguration + init))

    then:
      validations.isEmpty()

    where:
      init << ['foo123!@$%^&()-+=[];:~`', 'a_'].collect { [name: it] }
  }

  def "The recipe field is only required on creation"() {
    setup:
      Map baseConfiguration = [
          name      : 'foo',
          attributes: [any: 'any']
      ]
    when:
      def validations = validator.validate(new RepositoryXO(baseConfiguration), Create)

    then:
      validations.size() == 1
      validations[0].propertyPath.toString() == 'recipe'

    when:
      baseConfiguration.recipe = 'any'
      validations = validator.validate(new RepositoryXO(baseConfiguration), Create)

    then:
      validations.isEmpty()
  }

}
