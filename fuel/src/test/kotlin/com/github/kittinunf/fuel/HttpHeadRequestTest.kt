package com.github.kittinunf.fuel

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import org.hamcrest.CoreMatchers.`is` as isEqualTo

class HttpHeadRequestTest : BaseTestCase() {

    @Test
    fun httpHeadRequest() {
        var req: Request? = null
        var res: Response? = null
        var data: Any? = null
        var err: FuelError? = null

        "https://www.google.com".httpHead().responseString { request, response, (resData, resErr) ->
            req = request
            res = response
            data = resData
            err = resErr
        }

        assertThat(res?.httpStatusCode, isEqualTo(200))
        assertThat(req, notNullValue())
        assertThat(res, notNullValue())
        assertThat(err, nullValue())
        assertThat(data, notNullValue())
    }

}