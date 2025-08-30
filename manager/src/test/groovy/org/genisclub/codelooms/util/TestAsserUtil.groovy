import jakarta.validation.ValidationException
import org.geniusSociety.codelooms.common.exception.AssertException
import org.geniusSociety.codelooms.common.util.AssertUtil
import spock.lang.Specification
import spock.lang.Unroll

class TestAsserUtil extends Specification {
    @Unroll
    def "test isFalse with message: #message"() {
        when:
        AssertUtil.isFalse(expression, message)

        then:
        noExceptionThrown()

        where:
        expression | message
        false      | "Error message"
        false      | ["Error", " message"] as String[]
    }

    @Unroll
    def "test isFalse throws ValidationException when expression is true: #message"() {
        when:
        AssertUtil.isFalse(true, message)

        then:
        def e = thrown(ValidationException)
        e.message == expectedMessage

        where:
        message                       | expectedMessage
        "Error message"               | "Error message"
        ["Error", " msg"] as String[] | "Error msg"
    }

    @Unroll
    def "test isFalse with code and message: #message"() {
        when:
        AssertUtil.isFalse(expression, code, message)

        then:
        noExceptionThrown()

        where:
        expression | code | message
        false      | 400  | "Error message"
    }

    @Unroll
    def "test isFalse throws AssertException with code when expression is true: #message"() {
        when:
        AssertUtil.isFalse(true, code, message)

        then:
        def e = thrown(AssertException)
        e.code == code
        e.message == expectedMessage

        where:
        code | message                       | expectedMessage
        400  | "Error message"               | "Error message"
        500  | ["Error", " msg"] as String[] | "Error msg"
    }

    @Unroll
    def "test isTrue with message: #message"() {
        when:
        AssertUtil.isTrue(expression, message)

        then:
        noExceptionThrown()

        where:
        expression | message
        true       | "Success"
    }

    @Unroll
    def "test isTrue throws ValidationException when expression is false: #message"() {
        when:
        AssertUtil.isTrue(false, message)

        then:
        def e = thrown(ValidationException)
        e.message == expectedMessage

        where:
        message                       | expectedMessage
        "Error message"               | "Error message"
        ["Error", " msg"] as String[] | "Error msg"
    }

    @Unroll
    def "test isTrue with code and message: #message"() {
        when:
        AssertUtil.isTrue(expression, code, message)

        then:
        noExceptionThrown()

        where:
        expression | code | message
        true       | 200  | "Success"
    }

    @Unroll
    def "test isTrue throws AssertException with code when expression is false: #message"() {
        when:
        AssertUtil.isTrue(false, code, message)

        then:
        def e = thrown(AssertException)
        e.code == code
        e.message == expectedMessage

        where:
        code | message                       | expectedMessage
        400  | "Error message"               | "Error message"
        500  | ["Error", " msg"] as String[] | "Error msg"
    }

    def "test isAllTrue with message"() {
        when:
        AssertUtil.isAllTrue("All must be true", true, true, true)

        then:
        noExceptionThrown()
    }

    def "test isAllTrue throws ValidationException when any expression is false"() {
        when:
        AssertUtil.isAllTrue("All must be true", true, false, true)

        then:
        def e = thrown(ValidationException)
        e.message == "All must be true"
    }

    def "test isAllTrue with code and message"() {
        when:
        AssertUtil.isAllTrue("All must be true", 200, true, true)

        then:
        noExceptionThrown()
    }

    def "test isAllTrue throws AssertException with code when any expression is false"() {
        when:
        AssertUtil.isAllTrue("All must be true", 400, true, false)

        then:
        def e = thrown(AssertException)
        e.code == 400
        e.message == "All must be true"
    }

    def "test isAnyTrue throws AssertException when any expression is true"() {
        when:
        AssertUtil.isAnyTrue("None should be true", 400, false, true, false)

        then:
        def e = thrown(AssertException)
        e.code == 400
        e.message == "None should be true"
    }

    def "test notNull with message"() {
        when:
        AssertUtil.notNull(new Object(), "Object should not be null")

        then:
        noExceptionThrown()
    }

    def "test notNull throws ValidationException when object is null"() {
        when:
        AssertUtil.notNull(null, "Object is null")

        then:
        def e = thrown(ValidationException)
        e.message == "Object is null"
    }

    def "test isNull with message"() {
        when:
        AssertUtil.isNull(null, "Object should be null")

        then:
        noExceptionThrown()
    }

    def "test isNull throws ValidationException when object is not null"() {
        when:
        AssertUtil.isNull(new Object(), "Object is not null")

        then:
        def e = thrown(ValidationException)
        e.message == "Object is not null"
    }

    def "test throwMessage with varargs"() {
        when:
        AssertUtil.throwMessage("Error", " message")

        then:
        def e = thrown(ValidationException)
        e.message == "Error message"
    }

    def "test throwMessage with code and varargs"() {
        when:
        AssertUtil.throwMessage(500, "Server", " error")

        then:
        def e = thrown(AssertException)
        e.code == 500
        e.message == "Server error"
    }
}
