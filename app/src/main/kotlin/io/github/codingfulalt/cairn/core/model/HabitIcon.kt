package io.github.codingfulalt.cairn.core.model

// keys are stored in the database, don't rename them
enum class HabitIcon(
    val key: String,
) {
    Sparkle("sparkle"),
    Water("water"),
    Book("book"),
    Run("run"),
    Meditate("meditate"),
    Sleep("sleep"),
    Workout("workout"),
    Walk("walk"),
    Bike("bike"),
    Swim("swim"),
    Hike("hike"),
    Food("food"),
    Tea("tea"),
    Pill("pill"),
    Heart("heart"),
    Brain("brain"),
    Code("code"),
    Write("write"),
    Music("music"),
    Language("language"),
    Art("art"),
    Study("study"),
    Plant("plant"),
    Nature("nature"),
    Sun("sun"),
    Money("money"),
    Clean("clean"),
    Pet("pet"),
    Camera("camera"),
    NoPhone("no_phone"),
    ;

    companion object {
        fun fromKey(key: String?): HabitIcon = entries.firstOrNull { it.key == key } ?: Sparkle
    }
}
