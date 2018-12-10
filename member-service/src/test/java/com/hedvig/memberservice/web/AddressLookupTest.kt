package com.hedvig.memberservice.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.external.bisnodeBCI.BisnodeClient
import com.hedvig.external.bisnodeBCI.dto.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

@RunWith(SpringRunner::class)
@WebMvcTest(controllers = [AddressLookup::class])
class AddressLookupTest {

    val SSN = "191212121212"

    @Autowired
    private lateinit var mockMvc: MockMvc
    @Autowired
    private lateinit var objectMapper: ObjectMapper


    @MockBean
    lateinit var bisnodeClient: BisnodeClient

    @Test
    fun getAddressFromSwePersonnummer() {


        given(bisnodeClient.match(SSN)).willReturn(makeResponse())

        mockMvc.perform(
            makePostRequest(
                "/_/addresslookup/swe",
                mapOf(
                    "personummer" to SSN,
                    "memberId" to "1337"
                )
            )
        ).andExpect(
            status().is2xxSuccessful
        ).andExpect(
            jsonPath("firstName").value("Tolvan")
        ).andExpect(
            jsonPath("lastName").value("Tolvansson")
        )

    }

    //prefferredName can be null
    //Entrance can be null
    //Floor seems to always be null
    private fun makeResponse(): PersonSearchResultListResponse? {
        val response = PersonSearchResultListResponse(
            listOf(
                PersonSearchResult(
                    "random",
                    Person(
                        "gedi",
                        SSN,
                        false,
                        listOf("Tolvan"),
                        "Tolvan",
                        "Tolvansson",
                        "",
                        "",
                        Gender.Male,
                        LocalDate.parse("1912-12-12"),
                        false,
                        null,
                        false,
                        listOf(
                            Address(
                                "Visiting",
                                null,
                                "Street",
                                "13",
                                "A",
                                "1101",
                                null,
                                null,
                                "12345",
                                "Stockholm",
                                "SE",
                                listOf("Street 13 A 1101", "12345 Stockholm")
                            )
                        )
                    , listOf())
                )
            ))

        return response
    }


    private fun makePostRequest(url: String, body: Any): MockHttpServletRequestBuilder {

        return post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body))

    }
}