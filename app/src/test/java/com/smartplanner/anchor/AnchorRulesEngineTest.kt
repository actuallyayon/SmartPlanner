package com.smartplanner.anchor

import com.smartplanner.model.AnchorConfig
import com.smartplanner.model.AnchorType
import com.smartplanner.model.Habit
import com.smartplanner.model.anchor.AnchorRulesEngine
import com.smartplanner.model.anchor.FakeAnchorDetector
import com.smartplanner.model.anchor.ReminderEvaluationResult
import com.smartplanner.model.anchor.TimeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Deterministic test clock implementing TimeProvider.
 */
class FakeTimeProvider(
    var millis: Long = 1700000000000L,
    var dateString: String = "2026-09-21",
    var hourOfDay: Int = 14 // 2:00 PM (outside quiet hours)
) : TimeProvider {
    override fun currentTimeMillis(): Long = millis
    override fun currentDateString(): String = dateString
    override fun currentHourOfDay(): Int = hourOfDay

    fun advanceMinutes(minutes: Long) {
        millis += minutes * 60 * 1000
    }
}

class AnchorRulesEngineTest {

    private lateinit var fakeTime: FakeTimeProvider
    private lateinit var rulesEngine: AnchorRulesEngine

    @Before
    fun setUp() {
        fakeTime = FakeTimeProvider(
            millis = 1700000000000L,
            dateString = "2026-09-21",
            hourOfDay = 14
        )
        rulesEngine = AnchorRulesEngine(fakeTime)
    }

    private fun createHabit(
        lastReminderDate: String? = null,
        lastReminderTimestamp: Long? = null,
        quietStart: Int = 22,
        quietEnd: Int = 7,
        cooldownMinutes: Long = 120L
    ): Habit {
        return Habit(
            id = "habit_test",
            title = "Morning Coffee Stacking",
            trigger = "After brewing coffee",
            minVersion = "1 minute breathing",
            aiReasoning = "Gentle context trigger",
            anchorType = AnchorType.CHARGING_STARTED,
            anchorConfig = AnchorConfig(
                quietHoursStartHour = quietStart,
                quietHoursEndHour = quietEnd,
                cooldownMinutes = cooldownMinutes
            ),
            lastReminderTimestamp = lastReminderTimestamp,
            lastReminderDate = lastReminderDate
        )
    }

    @Test
    fun `evaluates Allowed when no previous reminders and outside quiet hours`() {
        val habit = createHabit()
        val result = rulesEngine.evaluate(habit)
        assertTrue("Expected Allowed result", result is ReminderEvaluationResult.Allowed)
    }

    @Test
    fun `suppresses reminder when already triggered today`() {
        val habit = createHabit(lastReminderDate = "2026-09-21")
        val result = rulesEngine.evaluate(habit)

        assertTrue(result is ReminderEvaluationResult.SuppressedAlreadyFiredToday)
        assertEquals("2026-09-21", (result as ReminderEvaluationResult.SuppressedAlreadyFiredToday).date)
    }

    @Test
    fun `allows reminder when last triggered on a previous day`() {
        val habit = createHabit(
            lastReminderDate = "2026-09-20",
            lastReminderTimestamp = fakeTime.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24 hours ago
        )
        val result = rulesEngine.evaluate(habit)
        assertTrue("Expected Allowed result on new day", result is ReminderEvaluationResult.Allowed)
    }

    @Test
    fun `suppresses reminder during active cooldown window`() {
        val habit = createHabit(
            lastReminderDate = "2026-09-20", // Previous day so daily check passes
            lastReminderTimestamp = fakeTime.currentTimeMillis() - (30 * 60 * 1000), // 30 min ago
            cooldownMinutes = 120L // 2 hour cooldown
        )
        val result = rulesEngine.evaluate(habit)

        assertTrue(result is ReminderEvaluationResult.SuppressedCooldown)
        val cooldown = result as ReminderEvaluationResult.SuppressedCooldown
        assertEquals(90L, cooldown.remainingMinutes)
    }

    @Test
    fun `allows reminder after cooldown period elapses`() {
        val habit = createHabit(
            lastReminderDate = "2026-09-20",
            lastReminderTimestamp = fakeTime.currentTimeMillis() - (130 * 60 * 1000), // 130 min ago
            cooldownMinutes = 120L
        )
        val result = rulesEngine.evaluate(habit)
        assertTrue("Expected Allowed after cooldown", result is ReminderEvaluationResult.Allowed)
    }

    @Test
    fun `suppresses reminder during late night quiet hours`() {
        fakeTime.hourOfDay = 23 // 11:00 PM (inside 22:00 -> 07:00 quiet hours)
        val habit = createHabit(quietStart = 22, quietEnd = 7)

        val result = rulesEngine.evaluate(habit)
        assertTrue(result is ReminderEvaluationResult.SuppressedQuietHours)
        val quiet = result as ReminderEvaluationResult.SuppressedQuietHours
        assertEquals(23, quiet.currentHour)
        assertEquals(22, quiet.startHour)
        assertEquals(7, quiet.endHour)
    }

    @Test
    fun `suppresses reminder during early morning quiet hours`() {
        fakeTime.hourOfDay = 5 // 5:00 AM (inside 22:00 -> 07:00 quiet hours)
        val habit = createHabit(quietStart = 22, quietEnd = 7)

        val result = rulesEngine.evaluate(habit)
        assertTrue(result is ReminderEvaluationResult.SuppressedQuietHours)
    }

    @Test
    fun `allows reminder right after quiet hours end`() {
        fakeTime.hourOfDay = 8 // 8:00 AM (after 07:00)
        val habit = createHabit(quietStart = 22, quietEnd = 7)

        val result = rulesEngine.evaluate(habit)
        assertTrue(result is ReminderEvaluationResult.Allowed)
    }

    @Test
    fun `fake detector correctly notifies listener when signal fires`() {
        val fakeDetector = FakeAnchorDetector(AnchorType.HEADPHONES_CONNECTED)
        var signalReceived: AnchorType? = null

        fakeDetector.startListening { detectedType ->
            signalReceived = detectedType
        }

        fakeDetector.simulateSignal()
        assertEquals(AnchorType.HEADPHONES_CONNECTED, signalReceived)

        fakeDetector.stopListening()
        signalReceived = null
        fakeDetector.simulateSignal()
        assertEquals("Should not receive signal after stopping", null, signalReceived)
    }
}
