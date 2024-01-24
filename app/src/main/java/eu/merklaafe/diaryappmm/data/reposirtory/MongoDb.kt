package eu.merklaafe.diaryappmm.data.reposirtory

import eu.merklaafe.diaryappmm.model.Diary
import eu.merklaafe.diaryappmm.util.Constants.APP_ID
import eu.merklaafe.diaryappmm.model.RequestState
import eu.merklaafe.diaryappmm.util.toInstant
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import java.time.ZoneId

object MongoDb: MongoRepository {
    private val app = App.Companion.create(APP_ID)
    private val user = app.currentUser
    private lateinit var realm: Realm

    init {
        configureRealm()
    }

    override fun configureRealm() {
        if(user != null) {
            val config = SyncConfiguration.Builder(user, setOf(Diary::class))
                .initialSubscriptions {sub ->
                    add(
                        query = sub.query<Diary>(query = "ownerId == $0", user.id),
                        name = "My Diaries"
                    )
                }
                .log(LogLevel.ALL)
                .build()
            realm = Realm.open(config)
        }
    }

    override fun getAllDiaries(): Flow<Diaries> {
        return if(user != null) {
            try {
                realm.query<Diary>(query = "ownerId == $0", user.id)
                    .sort(property = "date", sortOrder = Sort.DESCENDING)
                    .asFlow()
                    .map {result ->
                        RequestState.Success(
                            data = result.list.groupBy {
                                it.date.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                        )
                    }
            } catch(e: Exception) {
                flow {
                    emit(RequestState.Error(e))
                }
            }
        } else {
            flow {
                emit(RequestState.Error(UserNotAuthenticatedException()))
            }
        }
    }

    override fun getSelectedDiary(diaryId: ObjectId): Flow<RequestState<Diary>> {
        return if(user != null) {
            try {
                val diary = realm.query<Diary>(query = "_id == $0", diaryId).find().first()
                flow{ emit(RequestState.Success(data = diary)) }
            } catch (e: Exception) {
                flow{ emit(RequestState.Error(e)) }
            }
        } else {
            flow{ emit(RequestState.Error(UserNotAuthenticatedException())) }
        }
    }

    override suspend fun insertDiary(diary: Diary): RequestState<Diary> {
        return if(user != null) {
            realm.write {
                try {
                    val addedDiary = copyToRealm(diary.apply { ownerId = user.id })
                    RequestState.Success(data = addedDiary)
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun updateDiary(diary: Diary): RequestState<Diary> {
        return if(user != null) {
            realm.write {
                val queriedDiary = query<Diary>(query = "_id == $0", diary._id).find().first()
                if(queriedDiary != null) {
                    queriedDiary.title = diary.title
                    queriedDiary.description = diary.description
                    queriedDiary.mood = diary.mood
                    queriedDiary.images = diary.images
                    queriedDiary.date = diary.date
                    RequestState.Success(data = queriedDiary)
                } else {
                    RequestState.Error(error = Exception("Queried Diary does not exist."))
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }

    override suspend fun deleteDiary(diary: Diary): RequestState<Diary> {
        return if(user != null) {
            realm.writeBlocking {
                try {
                    val deletedDiary = realm.query<Diary>(query = "_id == $0 AND ownerId == $1", diary._id, user.id).find().first()
                    val liveDiary = findLatest(deletedDiary) ?: throw Exception("Diary not found in database.")
                    delete(liveDiary)
                    RequestState.Success(data = deletedDiary)
                } catch (e: Exception) {
                    RequestState.Error(e)
                }
            }
        } else {
            RequestState.Error(UserNotAuthenticatedException())
        }
    }
}

private class UserNotAuthenticatedException : Exception("User is not Logged in.")