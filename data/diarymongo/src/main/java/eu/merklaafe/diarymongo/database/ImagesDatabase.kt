package eu.merklaafe.diarymongo.database

import androidx.room.Database
import androidx.room.RoomDatabase
import eu.merklaafe.diarymongo.database.entity.ImageToDelete
import eu.merklaafe.diarymongo.database.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class, ImageToDelete::class],
    version = 2,
    exportSchema = false
)
abstract class ImagesDatabase: RoomDatabase() {
    abstract fun imageToUploadDao(): ImageToUploadDao
    abstract fun imageToDeleteDao(): ImageToDeleteDao
}