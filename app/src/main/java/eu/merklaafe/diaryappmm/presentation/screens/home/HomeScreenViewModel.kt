package eu.merklaafe.diaryappmm.presentation.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.merklaafe.diaryappmm.data.reposirtory.Diaries
import eu.merklaafe.diaryappmm.data.reposirtory.MongoDb
import eu.merklaafe.diaryappmm.util.RequestState
import kotlinx.coroutines.launch

class HomeScreenViewModel: ViewModel() {

    var diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)

    init {
        observerAllDiaries()
    }

    private fun observerAllDiaries() {
        diaries.value = RequestState.Loading
        viewModelScope.launch {
            MongoDb.getAllDiaries().collect() {result ->
                diaries.value = result
            }
        }
    }

}