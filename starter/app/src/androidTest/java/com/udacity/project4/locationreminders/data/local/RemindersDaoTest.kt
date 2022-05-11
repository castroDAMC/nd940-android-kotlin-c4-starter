package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()


    private fun getReminder(): ReminderDTO {
        return ReminderDTO(
            title = "TestTitle",
            description = "TestLocation",
            location = "fakeLocation",
            latitude = -3.062060,
            longitude = -60.025125)
    }

    @Test
    fun insertReminder_GetById_assertAllFields() = runBlockingTest {
        val fakeReminder = getReminder()
        database.reminderDao().saveReminder(fakeReminder)
        val reminderFromDB = database.reminderDao().getReminderById(fakeReminder.id)

        assertThat<ReminderDTO>(reminderFromDB as ReminderDTO, notNullValue())
        assertThat(reminderFromDB.id, `is`(fakeReminder.id))
        assertThat(reminderFromDB.title, `is`(fakeReminder.title))
        assertThat(reminderFromDB.description, `is`(fakeReminder.description))
        assertThat(reminderFromDB.latitude, `is`(fakeReminder.latitude))
        assertThat(reminderFromDB.longitude, `is`(fakeReminder.longitude))
        assertThat(reminderFromDB.location, `is`(fakeReminder.location))
    }

}