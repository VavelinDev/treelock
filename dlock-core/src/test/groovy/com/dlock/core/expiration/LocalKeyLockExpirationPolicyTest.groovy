package com.dlock.core.expiration

import com.dlock.core.expiration.LocalLockExpirationPolicy
import com.dlock.util.time.DateTimeProvider
import spock.lang.Specification

import java.time.LocalDateTime

/**
 * Tests of LocalLockExpirationPolicy.
 *
 * @author Przemyslaw Malirz
 */
class LocalKeyLockExpirationPolicyTest extends Specification {

    private LocalLockExpirationPolicy expirationPolicy

    def setup() {
        def dateTimeProvider = GroovyMock(DateTimeProvider.class) {
            now() >> LocalDateTime.parse("2019-01-01T12:00:00")
        }
        expirationPolicy = new LocalLockExpirationPolicy(dateTimeProvider)
    }

    def "Test expiration"(String createTimeString, Long expirationSeconds, boolean expired) {
        expect:
        def createTime = LocalDateTime.parse(createTimeString)
        expirationPolicy.expired(createTime, expirationSeconds) == expired

        where:
        createTimeString      | expirationSeconds | expired
        "2019-01-01T11:59:00" | 61                | false
        "2019-01-01T11:59:00" | 59                | true
        "2019-01-01T11:59:00" | 60                | false
    }

}
