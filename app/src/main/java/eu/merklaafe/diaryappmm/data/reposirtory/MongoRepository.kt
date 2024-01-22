package eu.merklaafe.diaryappmm.data.reposirtory

import eu.merklaafe.diaryappmm.model.Diary
import eu.merklaafe.diaryappmm.util.RequestState
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>

interface MongoRepository {
    fun configureRealm()
    fun getAllDiaries(): Flow<Diaries>
}