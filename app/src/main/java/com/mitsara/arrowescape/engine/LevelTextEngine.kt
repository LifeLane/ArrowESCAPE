package com.mitsara.arrowescape.engine

object LevelTextEngine {

    private val hooks = listOf(
        "Focus up.", "Easy mode.", "Don't blink.", "Watch the edges.",
        "Take a breath.", "One by one.", "Clear the path.", "Chain reaction.",
        "Think backwards.", "Find the loose end.", "Unknot this.", "Smooth moves.",
        "You got this.", "No rushing.", "Plan ahead.", "Read the board.",
        "Look closer.", "Stay sharp.", "Mind the gap.", "Perfect sequence."
    )

    private val funFacts = listOf(
        "Fun fact: You are 1% closer to becoming a puzzle master.",
        "Sarcastic fact: The arrows are just trying to get away from you.",
        "Did you know? Swiping harder doesn't make them move faster.",
        "Your brain just burned 0.01 calories.",
        "You survived... for now.",
        "Good job. Only 499 more to go.",
        "You solved it! The arrows are free. You are still here.",
        "Wow, you pressed a screen. Impressive.",
        "Fun fact: A goldfish could solve this in 3 days. You beat it.",
        "Are you procrastinating? Because this is a great way to do it.",
        "The board was secretly rooting for you.",
        "Genius level unlocked (not really, but keep going).",
        "Fact: This puzzle was afraid of you.",
        "Excellent. Now do it blindfolded.",
        "You missed a spot. Just kidding.",
        "This level is now retired to the puzzle hall of fame.",
        "You're on a roll. Don't let gravity stop you.",
        "Fact: 99% of arrows escape. The other 1% get stuck forever.",
        "That was smooth. Too smooth...",
        "Are we having fun yet?"
    )

    fun getHookForLevel(levelId: Int): String {
        return hooks[(levelId - 1) % hooks.size]
    }

    fun getFunFactForLevel(levelId: Int): String {
        return funFacts[(levelId - 1) % funFacts.size]
    }
}
