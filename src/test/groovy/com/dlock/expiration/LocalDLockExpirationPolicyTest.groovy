package com.dlock.expiration

import com.dlock.expiration.LocalDLockExpirationPolicy
import com.dlock.time.DateTimeProvider
import spock.lang.Specification

import java.time.LocalDateTime

/**
 * Tests of LocalDLockExpirationPolicy.
 *
 * @author Przemyslaw Malirz
 */
class LocalDLockExpirationPolicyTest extends Specification {

    private LocalDLockExpirationPolicy expirationPolicy

    def setup() {
        def dateTimeProvider = GroovyMock(DateTimeProvider.class) {
            now() >> LocalDateTime.parse("2019-01-01T12:00:00")
        }
        expirationPolicy = new LocalDLockExpirationPolicy(dateTimeProvider)
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
