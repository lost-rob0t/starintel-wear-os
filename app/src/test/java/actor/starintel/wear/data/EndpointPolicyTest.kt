package actor.starintel.wear.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class EndpointPolicyTest {
    @Test
    fun normalizesHttpsTrailingSlash() {
        assertEquals(
            "https://starintel.example",
            EndpointPolicy.normalize("  https://starintel.example/ "),
        )
    }

    @Test
    fun allowsPublicHttp() {
        assertEquals(
            "http://starintel.example:8080",
            EndpointPolicy.normalize("http://starintel.example:8080/"),
        )
    }

    @Test
    fun allowsLanHttp() {
        assertEquals(
            "http://192.168.1.40:8080",
            EndpointPolicy.normalize("http://192.168.1.40:8080"),
        )
    }

    @Test
    fun allowsLocalHostnameHttp() {
        assertEquals(
            "http://starintel.local",
            EndpointPolicy.normalize("http://starintel.local"),
        )
    }

    @Test
    fun marksHttpAsCleartext() {
        assertTrue(EndpointPolicy.isCleartext("http://10.0.0.4:8000"))
        assertFalse(EndpointPolicy.isCleartext("https://starintel.actor"))
    }

    @Test
    fun rejectsUnknownScheme() {
        assertThrows(IllegalArgumentException::class.java) {
            EndpointPolicy.normalize("ftp://starintel.example")
        }
    }
}
