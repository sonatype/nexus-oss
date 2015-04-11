package org.sonatype.nexus.common.validation

import org.junit.Before
import org.junit.Test
import org.sonatype.sisu.litmus.testsupport.TestSupport

import javax.validation.Valid
import javax.validation.Validation
import javax.validation.ValidatorFactory
import javax.validation.constraints.NotNull

/**
 * Bean validation trials.
 */
class BeanValidationTrial
    extends TestSupport
{
    def ValidatorFactory factory

    @Before
    void setUp() {
        factory = Validation.buildDefaultValidatorFactory()
    }

    class Parent
    {
        @Valid
        Child child
    }

    class Child
    {
        @Valid
        GrandChild grandChild
    }

    class GrandChild
    {
        @NotNull
        String name
    }

    @Test
    void 'validate hierarchy'() {
        def parent = new Parent(
            child: new Child(
                grandChild: new GrandChild(
                    name: null
                )
            )
        )
        def violations = factory.validator.validate(parent)
        log(violations)
        assert violations.size() == 1

        // child.grandChild.name
        def violation = violations.iterator().next()
        log(violation.propertyPath.class)
        violation.propertyPath.each {
            println it
            println it.class
            println it.name
            println it.index
            println it.key
            println it.kind
            println it.inIterable
            println '-'
        }
    }

    class WithMap
    {
        @Valid
        Map<String,Object> contents
    }

    @Test
    void 'validate with map'() {
        def parent = new Parent(
            child: new Child(
                grandChild: new GrandChild(
                    name: null
                )
            )
        )
        def withMap = new WithMap(contents: [ 'foo': parent ])
        def violations = factory.validator.validate(withMap)
        log(violations)
        assert violations.size() == 1

        // contents[foo].child.grandChild.name
        def violation = violations.iterator().next()
        log(violation.propertyPath.class)
        violation.propertyPath.each {
            println it
            println it.class
            println it.name
            println it.index
            println it.key
            println it.kind
            println it.inIterable
            println '-'
        }
    }
}
