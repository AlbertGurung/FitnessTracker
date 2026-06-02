package com.example.fitnesstracker.utilsTest

import com.example.fitnesstracker.data.user.ActivityLevel
import com.example.fitnesstracker.data.user.Gender
import com.example.fitnesstracker.data.user.GoalType
import com.example.fitnesstracker.data.user.UserProfile
import com.example.fitnesstracker.utils.CaloriesCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

/**
* TDD test class with JUnit tests for CaloriesCalculator, checking expected behaviour for different user profiles with different gender,
* activity level and weight goals to ensure correct calorie calculations.
*/
class CaloriesCalculatorTest {

    private val calculator = CaloriesCalculator() // Calculator instance to test.

    // Test a female user who is active and wants to lose weight.
    @Test
    fun testFemaleActiveLoseWeight() {
        val user = UserProfile(
            name = "Alice",
            height = 185.0,
            weight = 67.0,
            age = 25,
            gender = Gender.FEMALE,
            activityLevel = ActivityLevel.ACTIVE,
            goal = GoalType.LOSE_WEIGHT
        )
        val bmr = 10*67 + 6.25*185 - 5*25 - 161  // Calculate expected calories manually
        val expected = (bmr * 1.725 - 400).toInt()
        val result = calculator.calculateDailyCalories(user) // Run calculator and assert result
        assertEquals(expected, result)
    }

    // Test a female user who is very active and wants to gain weight
    @Test
    fun testFemaleVeryActiveGainWeight() {
        val user = UserProfile(
            name = "Isabella",
            height = 169.0,
            weight = 57.0,
            age = 22,
            gender = Gender.FEMALE,
            activityLevel = ActivityLevel.VERY_ACTIVE,
            goal = GoalType.GAIN_WEIGHT
        )
        val bmr = 10*57 + 6.25*169 - 5*22 - 161 // Calculate expected calories manually
        val expected = (bmr * 1.9 + 400).toInt()
        val result = calculator.calculateDailyCalories(user) // Run calculator and check correct behaviour with assert result
        assertEquals(expected, result)
    }

    // Test a male user who is sedentary and wants to maintain weight.
    @Test
    fun testMaleSedentaryMaintainWeight() {
        val user = UserProfile(
            name = "Bob",
            height = 175.0,
            weight = 65.0,
            age = 30,
            gender = Gender.MALE,
            activityLevel = ActivityLevel.SEDENTARY,
            goal = GoalType.MAINTAIN_WEIGHT
        )
        val expected = ((10*65 + 6.25*175 - 5*30 + 5) * 1.2).toInt()
        val result = calculator.calculateDailyCalories(user)
        assertEquals(expected, result)
    }
}
