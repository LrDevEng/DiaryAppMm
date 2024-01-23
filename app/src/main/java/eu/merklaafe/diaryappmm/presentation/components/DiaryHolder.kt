package eu.merklaafe.diaryappmm.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp
import eu.merklaafe.diaryappmm.model.Diary
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import eu.merklaafe.diaryappmm.model.Mood
import eu.merklaafe.diaryappmm.ui.theme.Elevation
import eu.merklaafe.diaryappmm.util.toInstant
import io.realm.kotlin.ext.realmListOf
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun DiaryHolder(
    diary: Diary,
    onClick: (String) -> Unit
) {
    val localDensity = LocalDensity.current
    var componentHeight by remember { mutableStateOf(0.dp) }
    var galleryOpened by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember {
                MutableInteractionSource()
            }
        ) {
            onClick(diary._id.toHexString())
        }
    ) {
        Spacer(modifier = Modifier.width(14.dp))
        Surface(
            modifier = Modifier
                .width(2.dp)
                .height(componentHeight + 14.dp),
            tonalElevation = Elevation.Level1
        ) {}
        Spacer(modifier = Modifier.width(20.dp))
        Surface(
            modifier = Modifier
                .clip(shape = Shapes().medium)
                .onGloballyPositioned {
                    componentHeight = with(localDensity) { it.size.height.toDp() }
                },
            tonalElevation = Elevation.Level1
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                DiaryHeader(
                    moodName = diary.mood,
                    time = diary.date.toInstant()
                )
                Text(
                    modifier = Modifier.padding(14.dp),
                    text = diary.description,
                    style = TextStyle(fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                if (diary.images.isNotEmpty()) {
                    ShowGalleryButton(
                        galleryOpened = galleryOpened,
                        galleryLoading = false,
                        onClick = {
                            galleryOpened = !galleryOpened
                        }
                    )
                }
                AnimatedVisibility(
                    visible = galleryOpened,
                    enter = fadeIn() + expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Gallery(images = diary.images)
                    }
                }
            }
        }
    }
}

@Composable
fun ShowGalleryButton(
    galleryOpened: Boolean,
    galleryLoading: Boolean,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick) {
        Text(
            text = if (galleryOpened)
                if (galleryLoading) "Loading" else "Hide Gallery"
            else "Show Gallery",
            style = TextStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize)
        )
    }
}

@Composable
fun DiaryHeader(moodName: String, time: Instant) {
    val mood by remember { mutableStateOf(Mood.valueOf(moodName)) }
    val formatter = remember {
        DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(mood.containerColor)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                modifier = Modifier.size(18.dp),
                painter = painterResource(id = mood.icon),
                contentDescription = "Mood Icon",
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = mood.name,
                color = mood.contentColor,
                style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
            )
        }
        Text(
            text = formatter.format(time),
            color = mood.contentColor,
            style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
        )
    }
}

@Preview
@Composable
fun DiaryHolderPreview() {
    DiaryHolder(
        diary = Diary().apply {
            title = "My Diary"
            description = "sahldfklsjfsahf alshf saf salhf saf jshlf sahf saäf safhsakjöfhsafsahf hsajh sahf sdhfh fh safhsa fb asfh sdahf sahf saf gsadfhsadhg#sa#jf sa ;)"
            mood = Mood.Mysterious.name
            images = realmListOf("","")
        },
        onClick = {}
    )
}