package eu.merklaafe.diaryappmm.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import eu.merklaafe.diaryappmm.data.database.entity.ImageToDelete
import eu.merklaafe.diaryappmm.data.database.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class, ImageToDelete::class],
    version = 2,
    exportSchema = false
)
abstract class ImagesDatabase: RoomDatabase() {
    abstract fun imageToUploadDao(): ImageToUploadDao
    abstract fun imageToDeleteDao(): ImageToDeleteDao
}