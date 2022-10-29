package com.singularitycoder.flowlauncher.helper

import com.singularitycoder.flowlauncher.BuildConfig
import com.singularitycoder.flowlauncher.R
import com.singularitycoder.flowlauncher.model.Quote
import com.singularitycoder.flowlauncher.model.QuoteColor

const val FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"
const val REQUEST_CODE_VIDEO = 1001
const val TAG_ADD_CONTACT_MODAL_BOTTOM_SHEET = "TAG_ADD_CONTACT_MODAL_BOTTOM_SHEET"

const val KEY_IS_WORK_COMPLETE = "KEY_IS_WORK_COMPLETE"
const val FIRST_URL = "FIRST_URL"

object Db {
    const val CONTACT = "db_contact"
}

object Table {
    const val CONTACT = "table_contact"
    const val NEWS = "table_news"
    const val WEATHER = "table_weather"
}

object Broadcast {
    const val TIME_CHANGED = "BROADCAST_TIME_CHANGED"
    const val PACKAGE_REMOVED = "BROADCAST_PACKAGE_REMOVED"
    const val PACKAGE_INSTALLED = "BROADCAST_PACKAGE_ADDED"
}

object WorkerTag {
    const val NEWS_PARSER = "WORKER_TAG_NEWS_PARSER"
    const val WEATHER_PARSER = "WORKER_TAG_WEATHER_PARSER"
}

enum class SpeechAction(val value: String) {
    NONE("none"),
    OPEN("open"),
    LAUNCH("launch"),
    CALL("call"),
    MESSAGE("message"),
    SEARCH("search"),
    FIND("find"),
}

val daysMap = mapOf(
    "mon" to "monday",
    "tue" to "tuesday",
    "wed" to "wednesday",
    "thu" to "thursday",
    "fri" to "friday",
    "sat" to "saturday",
    "sun" to "sunday",
)

val gradientList = listOf(
    R.drawable.gradient_default,
    R.drawable.gradient_red,
    R.drawable.gradient_pink,
    R.drawable.gradient_purple,
    R.drawable.gradient_deep_purple,
    R.drawable.gradient_indigo,
    R.drawable.gradient_blue,
    R.drawable.gradient_light_blue,
    R.drawable.gradient_cyan,
    R.drawable.gradient_teal,
    R.drawable.gradient_green,
    R.drawable.gradient_light_green,
    R.drawable.gradient_lime,
    R.drawable.gradient_yellow,
    R.drawable.gradient_amber,
    R.drawable.gradient_orange,
    R.drawable.gradient_deep_orange,
    R.drawable.gradient_brown,
    R.drawable.gradient_grey,
    R.drawable.gradient_blue_grey,
)

val quoteColorList = listOf(
    QuoteColor(
        textColor = R.color.purple_50,
        iconColor = R.color.purple_300,
        gradientColor = R.drawable.gradient_default
    ),
    QuoteColor(
        textColor = R.color.md_red_50,
        iconColor = R.color.md_red_400,
        gradientColor = R.drawable.gradient_red
    ),
    QuoteColor(
        textColor = R.color.md_pink_50,
        iconColor = R.color.md_pink_400,
        gradientColor = R.drawable.gradient_pink
    ),
    QuoteColor(
        textColor = R.color.md_purple_50,
        iconColor = R.color.md_purple_400,
        gradientColor = R.drawable.gradient_purple
    ),
    QuoteColor(
        textColor = R.color.md_deep_purple_50,
        iconColor = R.color.md_deep_purple_400,
        gradientColor = R.drawable.gradient_deep_purple
    ),
    QuoteColor(
        textColor = R.color.md_indigo_50,
        iconColor = R.color.md_indigo_400,
        gradientColor = R.drawable.gradient_indigo
    ),
    QuoteColor(
        textColor = R.color.md_blue_50,
        iconColor = R.color.md_blue_400,
        gradientColor = R.drawable.gradient_blue
    ),
    QuoteColor(
        textColor = R.color.md_light_blue_50,
        iconColor = R.color.md_light_blue_400,
        gradientColor = R.drawable.gradient_light_blue
    ),
    QuoteColor(
        textColor = R.color.md_cyan_50,
        iconColor = R.color.md_cyan_400,
        gradientColor = R.drawable.gradient_cyan
    ),
    QuoteColor(
        textColor = R.color.md_teal_50,
        iconColor = R.color.md_teal_400,
        gradientColor = R.drawable.gradient_teal
    ),
    QuoteColor(
        textColor = R.color.md_green_50,
        iconColor = R.color.md_green_400,
        gradientColor = R.drawable.gradient_green
    ),
    QuoteColor(
        textColor = R.color.md_light_green_50,
        iconColor = R.color.md_light_green_400,
        gradientColor = R.drawable.gradient_green
    ),
    QuoteColor(
        textColor = R.color.md_lime_50,
        iconColor = R.color.md_lime_400,
        gradientColor = R.drawable.gradient_lime
    ),
    QuoteColor(
        textColor = R.color.md_yellow_50,
        iconColor = R.color.md_yellow_400,
        gradientColor = R.drawable.gradient_yellow
    ),
    QuoteColor(
        textColor = R.color.md_amber_50,
        iconColor = R.color.md_amber_400,
        gradientColor = R.drawable.gradient_amber
    ),
    QuoteColor(
        textColor = R.color.md_deep_orange_50,
        iconColor = R.color.md_deep_orange_400,
        gradientColor = R.drawable.gradient_orange
    ),
    QuoteColor(
        textColor = R.color.md_deep_orange_50,
        iconColor = R.color.md_deep_orange_400,
        gradientColor = R.drawable.gradient_deep_orange
    ),
    QuoteColor(
        textColor = R.color.md_brown_50,
        iconColor = R.color.md_brown_400,
        gradientColor = R.drawable.gradient_brown
    ),
    QuoteColor(
        textColor = R.color.md_grey_50,
        iconColor = R.color.md_grey_400,
        gradientColor = R.drawable.gradient_grey
    ),
    QuoteColor(
        textColor = R.color.md_blue_grey_50,
        iconColor = R.color.md_blue_grey_400,
        gradientColor = R.drawable.gradient_blue_grey
    ),
)

val animeQuoteList = listOf(
    Quote(
        quote = "If the king doesn’t move, then his subjects won’t follow.",
        author = "Lelouch Vi Britannia"
    ),
    Quote(
        quote = "The only ones who should kill, are those who are prepared to be killed.",
        author = "Lelouch Vi Britannia"
    ),
    Quote(
        quote = "What do you do when there is an evil you cannot defeat by just means? Do you stain your hands with evil to destroy evil? Or do you remain steadfastly just and righteous even if it means surrendering to evil?",
        author = "Lelouch Vi Britannia"
    ),
    Quote(
        quote = """
            We wondered what happiness would look like if we could give it a physical form.
            If I'm not mistaken, I think it was Suzaku that said that the shape of happiness might resemble glass.
            His reasoning made sense. He said that even though you don't usually notice it, it's still definitely there.
            You merely have to change your point of view slightly, and then that glass will sparkle when it reflects the light.
            I doubt that anything else could argue its own existence more eloquently.
        """.trimIndentsAndNewLines(),
        author = "Lelouch Vi Britannia"
    ),
    Quote(
        quote = "If strength is justice, then is powerlessness a crime?",
        author = "Lelouch Vi Britannia"
    ),
    Quote(
        quote = "You can't change the world without getting your hands dirty.",
        author = "Lelouch Vi Britannia"
    ),
    Quote(
        quote = "The trick of real combat is that everyone is human.",
        author = "Lelouch Vi Britannia"
    ),
    Quote(
        quote = """
            Perhaps this is what I have always wished for since that day.
            The loss and destruction of all. That's right, one must destroy before creating.
            In that case, if my conscience becomes a hindrance to me, then I will simply erase it.
            I have no other choice but to move forward.
        """.trimIndentsAndNewLines(),
        author = "Lelouch Vi Britannia"
    ),
    Quote(
        quote = """
            All the hatred in the world is gathered on me, as promised.
            So, all you have to do is to erase my existence, and put an end to this chain of hatred.
            The Black Knights will have the legend of Zero left behind for them.
            Schneizel will work for Zero.
            And now the world can be unified at one table, not through military force, but through negotiation and talk.
            Mankind can finally embrace the future.
        """.trimIndentsAndNewLines(),
        author = "Lelouch Vi Britannia"
    ),
    Quote(
        quote = "Yes... I... Destroy the world... and create it... anew.",
        author = "Lelouch Vi Britannia"
    ),
    Quote(
        quote = "Emperor Charles sought the past, you seek the present, but I seek the future!",
        author = "Lelouch Vi Britannia"
    ),
    Quote(
        quote = "The brighter the light shines, the darker the nearby shadows grow!",
        author = "Berserk"
    ),
    Quote(
        quote = "If you gaze long into an abyss, the abyss also gazes into you.",
        author = "Made In Abyss"
    ),
    Quote(
        quote = "Emotions are a luxury.",
        author = "Escort Warrior"
    ),
    Quote(
        quote = "Once words have been planted they cannot be removed. From those words, thoughts bloom like a flower and constantly linger.",
        author = "Unknown"
    ),
    Quote(
        quote = "Even if it's one thing, know it properly! And Perfectly!",
        author = "Peerless Dad"
    ),
    Quote(
        quote = "The difference between almost knowing something and actually knowing something are as far apart as the sky and the ground!",
        author = "Peerless Dad"
    ),
    Quote(
        quote = "You can only see as much as you know!",
        author = "Peerless Dad"
    ),
    Quote(
        quote = """
            In the past, someone tried to avenge his father by sleeping on firewood and suffering.
            Did he do it because it helped in getting his revenge? Fool! How would that help?
            He did it so that he wouldn't forget. Because humans are sly.
            They want to sit down when they are standing up.
            When they are sitting down, they want to lie down.
            And when they lie down, they would want to sleep.
        """.trimIndentsAndNewLines(),
        author = "Peerless Dad"
    ),
    Quote(
        quote = "Luck is also a skill.",
        author = "Escort Warrior"
    ),
    Quote(
        quote = "The farther you look, the wider the world is.",
        author = "Escort Warrior"
    ),
    Quote(
        quote = """
            There is only a slight difference between poison and medicine!
            Even if a medicine good for your health is taken too much, it becomes poison.
            And if you take the right amount of poison that is bad for health, it becomes a medicine!
        """.trimIndentsAndNewLines(),
        author = "Escort Warrior"
    ),
    Quote(
        quote = "Absolute power indeed trumps over any ploy!",
        author = "Magic Emperor"
    ),
    Quote(
        quote = """
            If you don't practice for a day, only you will know.
            Two days, your peers will know.
            Three days, the audience will know.
        """.trimIndentsAndNewLines(),
        author = "Dance Dance Danseur"
    ),
    Quote(
        quote = "The wise control others, while the strong get controlled by others.",
        author = "Magic Emperor"
    ),
    Quote(
        quote = "If you learn a day late, you'll be 10 days late in understanding it.",
        author = "Peerless Dad"
    ),
    Quote(
        quote = """
            It's the difference between doing things when you understand why its being done
            and doing things when you don't understand it.
        """.trimIndentsAndNewLines(),
        author = "Peerless Dad"
    ),
    Quote(
        quote = "No matter how busy you are, you must take time to make other person feel important.",
        author = "Unknown"
    ),
    Quote(
        quote = "If you spend too much time thinking about a thing, you'll never get it done.",
        author = "Unknown"
    ),
    Quote(
        quote = "Time is what we want most, but what we use worst.",
        author = "Unknown"
    ),
    Quote(
        quote = "With tolerance comes greatness.",
        author = "Magic Emperor"
    ),
    Quote(
        quote = "Patience and time do more than strength or passion.",
        author = "Unknown"
    ),
    Quote(
        quote = "Time is the most valuable thing a man can spend.",
        author = "Unknown"
    ),
    Quote(
        quote = "Lost time is never found again.",
        author = "Unknown"
    ),
    Quote(
        quote = "Ignorance isn't bliss. Ignorance is weakness. Ignorance is a sin.",
        author = "Spy X Family"
    ),
    Quote(
        quote = "Information. The strongest and most essential weapon among all weapons.",
        author = "Spy X Family"
    ),
    Quote(
        quote = """
            A university research went like this. The researchers showed the test subjects a film of a guy getting slapped.
            The test subjects showed signs of discomfort cuz their brains empathized with the guy's pain.
            And that means it's human nature to want to avoid violence. But the thing is, they ran the experiment again,
            and this time they told the subjects, "The guy's getting slapped by his lover because he cheated on her."
            So what do you think happened? When they watched it, the subject's brains showed signs of pleasure!
            I mean, doesn't it freak you out? They have no idea if they are being told the truth.
            But once the idea is in their heads, they do a complete 180. Weird right? Aren't wars waged in the same fashion.
            They tell us to hate each other by giving us a reason. So we fight. And then we die. It's the most pointless thing in the world.
        """.trimIndentsAndNewLines(),
        author = "Spy X Family"
    ),
    Quote(
        quote = "Talent is something you bloom, Instinct is something you polish.",
        author = "Haikyuu"
    ),
    Quote(
        quote = "When two fight, the third wins.",
        author = "Magic Emperor"
    ),
    Quote(
        quote = "Better three hours too soon than a minute too late.",
        author = "Unknown"
    ),
    Quote(
        quote = "Talent is not something you are born with, but something you train and polish.",
        author = "Ya Boy Kongming!"
    ),
    Quote(
        quote = "Friction turns stones into jewels.",
        author = "Ya Boy Kongming!"
    ),
    Quote(
        quote = "Letting your enemy set the terms of engagement is akin to entering your enemy's territory.",
        author = "Ya Boy Kongming!"
    ),
    Quote(
        quote = "Decide first and act to make it happen.",
        author = "Peerless Dad"
    ),
    Quote(
        quote = "You know what they say. Fool me once, shame on you. Fool me twice, shame on me.",
        author = "Unknown"
    ),
    Quote(
        quote = "Don’t confuse movement with progress.",
        author = "Unknown"
    ),
    Quote(
        quote = "It is satisfying to see a plan progress accordingly.",
        author = "Unknown"
    ),
    Quote(
        quote = "Power results from self control.",
        author = "Unknown"
    ),
    Quote(
        quote = "What doesn't kill you makes you stronger.",
        author = "Kaguya-sama: Love Is War - Ultra Romantic"
    ),
    Quote(
        quote = "We must use time wisely and forever realise that the time is always ripe to do right.",
        author = "Unknown"
    ),
    Quote(
        quote = """
            You are weak. You try to manipulate things to show off your strength.
            In this way, you cover your weakness by trampling down the others.
        """.trimIndentsAndNewLines(),
        author = "Skeleton Knight"
    ),
    Quote(
        quote = "Weakness makes you desire superficial strength.",
        author = "Skeleton Knight"
    ),
    Quote(
        quote = """
            The genuinely strong people won't be carried away by their strength and
            will use their strength to protect the weak without hesitation.
        """.trimIndentsAndNewLines(),
        author = "Skeleton Knight"
    ),
    Quote(
        quote = """
            Don't view everything in terms of black or white.
            Don't try to make everything fit into your preconceived notions and don't rationalize.
            You must act based on objective reasoning
        """.trimIndentsAndNewLines(),
        author = "Spy X Family"
    ),
    Quote(
        quote = "Nothing of import is ever accomplished in a day.",
        author = "Spy X Family"
    ),
    Quote(
        quote = """
            Hard times create strong men.
            Strong men create good times.
            Good times create weak men.
            Weak men create hard times.
        """.trimIndentsAndNewLines(),
        author = "Unknown"
    ),
    Quote(
        quote = "If one's intentions are transparent, others will reciprocate. That's how you maintain peace.",
        author = "Peerless Dad"
    ),
    Quote(
        quote = "Hell may be before you. But you are still alive as long as you haven't set foot inside it.",
        author = "Chronicles of heavenly demon"
    ),
    Quote(
        quote = """
            If you are a martial artist, you should know how meaningless it is to look for the easy way.
            Look for the easy way and you might as well find the easy way to death.
            This is common sense for a martial artist who lives with a weapon in their hand.
        """.trimIndentsAndNewLines(),
        author = "Chronicles of heavenly demon"
    ),
    Quote(
        quote = "There is no such thing as easy training.",
        author = "Chronicles of heavenly demon"
    ),
    Quote(
        quote = "Wisdom only comes after continuously reviewing the knowledge and experience one has newly achieved.",
        author = "Chronicles of heavenly demon"
    ),
    Quote(
        quote = "Simply knowing and understanding are two very different things.",
        author = "Chronicles of heavenly demon"
    ),
    Quote(
        quote = "All human behaviour is determined by Goals.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = """
            Being in a pinch is a an opportunity. That is my motto.
            You can always turn the tables when you are in a dangerous situation.
            That’s why no matter when or whom you are fighting,
            not giving up and rising to the challenge is what matters most.
        """.trimIndentsAndNewLines(),
        author = "Isekai Ojisan"
    ),
    Quote(
        quote = "God only gives us what we can endure.",
        author = "Other World Warrior"
    ),
    Quote(
        quote = "Unless I have personally experienced it, I refuse to let rumors cloud my judgment of a person.",
        author = "Other World Warrior"
    ),
    Quote(
        quote = "Truth is mighty and it always prevails in the end.",
        author = "Detective Conan"
    ),
    Quote(
        quote = "Know thyself and your enemy and you’d be ever-victorious.",
        author = "Magic Emperor"
    ),
    // A great humble answer by the King IMO. He neither denies the praise nor takes it in arrogantly.
    Quote(
        quote = """
            Witch: You are indeed a rare and extraordinary person.
            King: Only because I stand on the predecessors shoulders.
        """.trimIndentsAndNewLines(),
        author = "Burn That Witch"
    ),
    Quote(
        quote = "I will keep moving forward until I exterminate the enemy.",
        author = "Eren Yeager"
    ),
    Quote(
        quote = "I pursue perfection! The perfection which is alone sublime in its beauty.",
        author = "The Boxer"
    ),
    Quote(
        quote = """
            I pursue perfection! The perfection which is alone sublime in its beauty.
            There is no nervousness about the fight, desire to win, or fear of losing.
            There is no past, present, or future. None of that exists.
            It is to obtain full control of my muscles!
            From the lightest and thickest muscles down to every minute muscle fibre from
            the tips of my fingers down to the tips of my toes.
        """.trimIndentsAndNewLines(),
        author = "The Boxer"
    ),
    Quote(
        quote = """
            Q: What is boxing to you?
            A: The pursuit of beauty.
        """.trimIndent(),
        author = "The Boxer"
    ),
    Quote(
        quote = """
            There are limits to a battle in solitude.
            There are limits to what one person can do alone.
            There is a limit on how long you can fight alone.
        """.trimIndentsAndNewLines(),
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "Every failure is a step to success.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "Doubting everything or believing everything are two equally convieient solutions, both of which exempt us from thinking.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "Doing things before they are done to you is the basic tactic.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "Humans are psychologically premature creatures, but that also means they also carry unknown potential.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "Truth. Its powerful but its a double-edged sword.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "When one is plunged into a crisis, they only see and hear what they want to.",
        author = "Peerless Dad"
    ),
    Quote(
        quote = "In war, the obvious path out is the more dangerous path.",
        author = "Peerless Dad"
    ),
    Quote(
        quote = "Why would I hate those who are weaker than myself? I only pity them.",
        author = "Lion Sin - Escanor"
    ),
    Quote(
        quote = "A man who cannot command himself always remains a slave.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "Trauma is awakened more strongly by experience than by word.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "Thoughtless power crumbles by its own mass.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "You reap what you sow.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "Violence is the most powerful force in the world.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "This world is ruled by violence. True strength in this world is determined by the power of violence. There is nothing that can overrule that truth. Nothing, except death.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "Fear and pleasure are two sides of the same coin. The difference is paper-thin in this world.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "Pain turns into fear over time.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "As usual, you have mistaken isolation for independence.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "Seek life when facing death.",
        author = "Overlord"
    ),
    Quote(
        quote = "The worst enemy you can meet will always be yourself.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "Tyrants are only allowed to rule as long as their power makes sense.",
        author = "Classroom of the Elite"
    ),
    Quote(
        quote = "Smiling means letting your guard down in front of people even if just a little.",
        author = "Classroom of the Elite"
    ),
)