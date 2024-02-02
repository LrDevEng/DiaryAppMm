package eu.merklaafe.diaryutil.model

import androidx.compose.ui.graphics.Color
import eu.merklaafe.diaryui.theme.AngryColor
import eu.merklaafe.diaryui.theme.AwfulColor
import eu.merklaafe.diaryui.theme.BoredColor
import eu.merklaafe.diaryui.theme.CalmColor
import eu.merklaafe.diaryui.theme.DepressedColor
import eu.merklaafe.diaryui.theme.DisappointedColor
import eu.merklaafe.diaryui.theme.HappyColor
import eu.merklaafe.diaryui.theme.HumorousColor
import eu.merklaafe.diaryui.theme.LonelyColor
import eu.merklaafe.diaryui.theme.MysteriousColor
import eu.merklaafe.diaryui.theme.NeutralColor
import eu.merklaafe.diaryui.theme.RomanticColor
import eu.merklaafe.diaryui.theme.ShamefulColor
import eu.merklaafe.diaryui.theme.SurprisedColor
import eu.merklaafe.diaryui.theme.SuspiciousColor
import eu.merklaafe.diaryui.theme.TenseColor
import eu.merklaafe.diaryutil.R

enum class Mood(
    val icon: Int,
    val contentColor: Color,
    val containerColor: Color
) {
    Neutral(
        icon = R.drawable.neutral,
        contentColor = Color.Black,
        containerColor = NeutralColor
    ),
    Happy(
        icon = R.drawable.happy,
        contentColor = Color.Black,
        containerColor = HappyColor
    ),
    Angry(
        icon = R.drawable.angry,
        contentColor = Color.White,
        containerColor = AngryColor
    ),
    Bored(
        icon = R.drawable.bored,
        contentColor = Color.Black,
        containerColor = BoredColor
    ),
    Calm(
        icon = R.drawable.calm,
        contentColor = Color.Black,
        containerColor = CalmColor
    ),
    Depressed(
        icon = R.drawable.depressed,
        contentColor = Color.Black,
        containerColor = DepressedColor
    ),
    Disappointed(
        icon = R.drawable.disappointed,
        contentColor = Color.White,
        containerColor = DisappointedColor
    ),
    Humorous(
        icon = R.drawable.humorous,
        contentColor = Color.Black,
        containerColor = HumorousColor
    ),
    Lonely(
        icon = R.drawable.lonely,
        contentColor = Color.White,
        containerColor = LonelyColor
    ),
    Mysterious(
        icon = R.drawable.mysterious,
        contentColor = Color.Black,
        containerColor = MysteriousColor
    ),
    Romantic(
        icon = R.drawable.romantic,
        contentColor = Color.White,
        containerColor = RomanticColor
    ),
    Shameful(
        icon = R.drawable.shameful,
        contentColor = Color.White,
        containerColor = ShamefulColor
    ),
    Awful(
        icon = R.drawable.awful,
        contentColor = Color.Black,
        containerColor = AwfulColor
    ),
    Surprised(
        icon = R.drawable.surprised,
        contentColor = Color.Black,
        containerColor = SurprisedColor
    ),
    Suspicious(
        icon = R.drawable.suspicious,
        contentColor = Color.Black,
        containerColor = SuspiciousColor
    ),
    Tense(
        icon = R.drawable.tense,
        contentColor = Color.Black,
        containerColor = TenseColor
    )
}