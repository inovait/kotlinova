package si.inova.kotlinova.coroutines

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Tasks
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import si.inova.kotlinova.testing.ImmediateDispatcherRule
import si.inova.kotlinova.utils.createLocation

class LocationCoroutinesTest {
    private lateinit var locationClient: FusedLocationProviderClient

    @get:Rule
    val coroutinesRule = ImmediateDispatcherRule()
    @get:Rule
    val archRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        locationClient = mock()
    }

    @Test
    fun getLocation() = runBlocking<Unit> {
        val location = createLocation(10.0, 11.0)

        whenever(locationClient.requestLocationUpdates(any(), any(), anyOrNull())).thenAnswer {
            val locationCallback = it.arguments[1] as LocationCallback
            locationCallback.onLocationResult(LocationResult.create(listOf(location)))

            Tasks.forResult(Unit)
        }

        val locationRequest = LocationRequest.create()

        assertEquals(location, locationClient.awaitSingleLocation(locationRequest))

        val callbackCaptor = argumentCaptor<LocationCallback>()
        verify(locationClient)
            .requestLocationUpdates(eq(locationRequest), callbackCaptor.capture(), anyOrNull())

        val callback = callbackCaptor.firstValue
        verify(locationClient).removeLocationUpdates(callback)
    }

    @Test
    fun removeLocationUpdatesAfterUnsubscribe() = runBlocking<Unit> {
        val locationRequest = LocationRequest.create()

        val locationFetcher = async(Unconfined) {
            locationClient.awaitSingleLocation(locationRequest)
        }

        val callbackCaptor = argumentCaptor<LocationCallback>()
        verify(locationClient)
            .requestLocationUpdates(eq(locationRequest), callbackCaptor.capture(), anyOrNull())

        verify(locationClient, never()).removeLocationUpdates(any<LocationCallback>())
        val callback = callbackCaptor.firstValue

        locationFetcher.cancel()
        verify(locationClient).removeLocationUpdates(callback)
    }
}