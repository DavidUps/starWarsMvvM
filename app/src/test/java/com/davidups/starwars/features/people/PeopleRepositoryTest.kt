package com.davidups.starwars.features.people

import android.content.SharedPreferences
import com.davidups.starwars.UnitTest
import com.davidups.starwars.core.exception.Failure
import com.davidups.starwars.core.functional.Either
import com.davidups.starwars.core.platform.NetworkHandler
import com.davidups.starwars.features.people.models.People
import com.davidups.starwars.features.people.models.PeopleEntity
import com.davidups.starwars.features.people.models.Person
import com.davidups.starwars.features.people.models.PersonEntity
import com.davidups.starwars.features.people.services.PeopleLocal
import com.davidups.starwars.features.people.services.PeopleService
import com.davidups.starwars.features.people.usecases.PeopleRepository
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.talentoMobile.starwars.features.fetch.services.FetchLocal
import io.kotlintest.matchers.shouldEqual
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import retrofit2.Call
import retrofit2.Response
import java.net.CacheResponse

class PeopleRepositoryTest : UnitTest() {

    private lateinit var networkRepository: PeopleRepository.Network
    @Mock private lateinit var networkHandler: NetworkHandler
    @Mock private lateinit var service: PeopleService

    @Mock private lateinit var peopleCall: Call<PeopleEntity>
    @Mock private lateinit var peopleResponse: Response<PeopleEntity>

    @Mock private lateinit var peopleLocal: PeopleLocal
    @Mock private lateinit var fetchLocal: FetchLocal

    @Before
    fun setUp(){
        networkRepository = PeopleRepository.Network(networkHandler, service, peopleLocal, fetchLocal)
    }

    @Test fun `should return empty list by default`() {
        given { networkHandler.isConnected }.willReturn(true)
        given { peopleResponse.body() }.willReturn(null)
        given { peopleResponse.isSuccessful }.willReturn(true)
        given { peopleCall.execute() }.willReturn(peopleResponse)
        given { service.getPeople() }.willReturn(peopleCall)

        val people = networkRepository.getPeople()

        people shouldEqual Either.Right(emptyList<People>())
        verify(service).getPeople()
    }

    @Test fun `should get people list from service`() {
        given { networkHandler.isConnected }.willReturn(true)
        given { peopleResponse.body() }.willReturn(
            PeopleEntity(
                1,
                "siguiente",
                "anterior",
                arrayListOf(
                    PersonEntity(
                        0,
                        "1996",
                        "Marrón",
                        "Masculino",
                        "Marrón",
                        180,
                        "https://www.google.es",
                        75,
                        "David",
                        "blanca"
                    )
                ))
        )
        given { peopleResponse.isSuccessful }.willReturn(true)
        given { peopleCall.execute() }.willReturn(peopleResponse)
        given { service.getPeople() }.willReturn(peopleCall)

        val articles = networkRepository.getPeople()

        articles shouldEqual Either.Right(listOf(
            Person(
                "1996",
                "Marrón",
                "Masculino",
                "Marrón",
                180,
                "https://www.google.es",
                75,
                "David",
                "blanca"
            )))
        verify(service).getPeople()
    }

    @Test
    fun `people service should return network failure when no connection`() {
        given { networkHandler.isConnected }.willReturn(false)

        val movies = networkRepository.getPeople()

        movies shouldBeInstanceOf Either::class.java
        movies.isLeft shouldEqual true
        movies.either({ failure -> failure shouldBeInstanceOf Failure.NetworkConnection::class.java }, {})
        verifyZeroInteractions(service)
    }

    @Test
    fun `people service should return network failure when undefined connection`() {
        given { networkHandler.isConnected }.willReturn(null)

        val movies = networkRepository.getPeople()

        movies shouldBeInstanceOf Either::class.java
        movies.isLeft shouldEqual true
        movies.either({ failure -> failure shouldBeInstanceOf Failure.NetworkConnection::class.java }, {})
        verifyZeroInteractions(service)
    }

    @Test
    fun `people service should return server error if no successful response`() {
        given { networkHandler.isConnected }.willReturn(true)

        val movies = networkRepository.getPeople()

        movies shouldBeInstanceOf Either::class.java
        movies.isLeft shouldEqual true
        movies.either({ failure -> failure shouldBeInstanceOf Failure.ServerError::class.java }, {})
    }
}