package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {

        //    TODO: Create a fake data source to act as a double to the real data source
        private var shouldReturnError = false

        fun setShouldReturnError(shouldReturn: Boolean) {
            this.shouldReturnError = shouldReturn
        }

        override suspend fun saveReminder(reminder: ReminderDTO) {
            reminders.add(reminder)
        }

        override suspend fun deleteAllReminders() {
            reminders.clear()
        }

        override suspend fun getReminders(): Result<List<ReminderDTO>> {
            return when(shouldReturnError){
                true -> Result.Error("Error")
                else -> Result.Success(ArrayList(reminders))
            }
        }

        override suspend fun getReminder(id: String): Result<ReminderDTO> {

            return when(shouldReturnError){
                true -> Result.Error("Error")
                else -> {
                    val reminder = reminders.find { it.id == id }

                    if (reminder != null) {
                        Result.Success(reminder)
                    } else {
                        Result.Error("Reminder not found", 404)
                    }
                }
            }
        }
}