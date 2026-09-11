package io.github.codingfulalt.cairn.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsBike
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EmojiFoodBeverage
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Hiking
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PhonelinkOff
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Pool
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.codingfulalt.cairn.core.model.HabitIcon

val HabitIcon.imageVector: ImageVector
    get() =
        when (this) {
            HabitIcon.Sparkle -> Icons.Rounded.AutoAwesome
            HabitIcon.Water -> Icons.Rounded.WaterDrop
            HabitIcon.Book -> Icons.AutoMirrored.Rounded.MenuBook
            HabitIcon.Run -> Icons.AutoMirrored.Rounded.DirectionsRun
            HabitIcon.Meditate -> Icons.Rounded.SelfImprovement
            HabitIcon.Sleep -> Icons.Rounded.Bedtime
            HabitIcon.Workout -> Icons.Rounded.FitnessCenter
            HabitIcon.Walk -> Icons.AutoMirrored.Rounded.DirectionsWalk
            HabitIcon.Bike -> Icons.AutoMirrored.Rounded.DirectionsBike
            HabitIcon.Swim -> Icons.Rounded.Pool
            HabitIcon.Hike -> Icons.Rounded.Hiking
            HabitIcon.Food -> Icons.Rounded.Restaurant
            HabitIcon.Tea -> Icons.Rounded.EmojiFoodBeverage
            HabitIcon.Pill -> Icons.Rounded.Medication
            HabitIcon.Heart -> Icons.Rounded.Favorite
            HabitIcon.Brain -> Icons.Rounded.Psychology
            HabitIcon.Code -> Icons.Rounded.Code
            HabitIcon.Write -> Icons.Rounded.EditNote
            HabitIcon.Music -> Icons.Rounded.MusicNote
            HabitIcon.Language -> Icons.Rounded.Translate
            HabitIcon.Art -> Icons.Rounded.Brush
            HabitIcon.Study -> Icons.Rounded.School
            HabitIcon.Plant -> Icons.Rounded.LocalFlorist
            HabitIcon.Nature -> Icons.Rounded.Park
            HabitIcon.Sun -> Icons.Rounded.WbSunny
            HabitIcon.Money -> Icons.Rounded.Savings
            HabitIcon.Clean -> Icons.Rounded.CleaningServices
            HabitIcon.Pet -> Icons.Rounded.Pets
            HabitIcon.Camera -> Icons.Rounded.PhotoCamera
            HabitIcon.NoPhone -> Icons.Rounded.PhonelinkOff
        }
