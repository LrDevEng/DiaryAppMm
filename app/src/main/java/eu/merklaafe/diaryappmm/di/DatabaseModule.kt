package eu.merklaafe.diaryappmm.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eu.merklaafe.diaryutil.connectivity.NetworkConnectivityObserver
import javax.inject.Singleton
import eu.merklaafe.diarymongo.database.ImagesDatabase
import eu.merklaafe.diaryutil.Constants.IMAGES_DATABASE

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): ImagesDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = ImagesDatabase::class.java,
            name = IMAGES_DATABASE
        ).build()
    }

    @Provides
    @Singleton
    fun provideFirstDao(database: ImagesDatabase) = database.imageToUploadDao()

    @Provides
    @Singleton
    fun provideSecondDao(database: ImagesDatabase) = database.imageToDeleteDao()

    @Provides
    @Singleton
    fun provideNetworkConnectivityObserver(@ApplicationContext context: Context) = NetworkConnectivityObserver(context = context)
}