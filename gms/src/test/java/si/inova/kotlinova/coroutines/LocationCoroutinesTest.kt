/*
 * Copyright 2020 INOVA IT d.o.o.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package si.inova.kotlinova.coroutines

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
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

        // UNCONFINED is experimental, but it is still fine to use it with tests
        @Suppress("EXPERIMENTAL_API_USAGE")
        val locationFetcher = async(Dispatchers.Unconfined) {
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