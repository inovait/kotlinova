package si.inova.kotlinova.retrofit

import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class DefaultResponseParserTest {
    @Test
    fun `Parse response normally`() {
        val response = Response.success("Test")

        assertEquals("Test", DefaultResponseParser.parseResponse(response))
    }

    @Test(expected = HttpException::class)
    fun `Parse error`() {
        val response = Response.error<String>(
            500, ResponseBody.create(null, "")
        )

        DefaultResponseParser.parseResponse(response)
    }

    @Test
    fun `Parse empty response`() {
        val response = Response.success<Unit>(null)

        assertEquals(Unit, DefaultResponseParser.parseResponse(response))
    }
}