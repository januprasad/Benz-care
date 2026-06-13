package com.toyota.demo.di

import android.content.Context
import com.toyota.demo.data.db.ToyotaDao
import com.toyota.demo.data.db.ToyotaDatabase
import com.toyota.demo.data.repository.ToyotaRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ToyotaDatabase {
        return ToyotaDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideDao(database: ToyotaDatabase): ToyotaDao {
        return database.toyotaDao()
    }

    @Provides
    @Singleton
    fun provideScanDao(database: ToyotaDatabase): com.toyota.demo.data.db.ScanDao {
        return database.scanDao()
    }

    @Provides
    @Singleton
    fun provideRepository(dao: ToyotaDao): ToyotaRepository {
        return ToyotaRepository(dao)
    }

    @Provides
    @Singleton
    fun provideScanRepository(dao: com.toyota.demo.data.db.ScanDao): com.toyota.demo.data.repository.ScanRepository {
        return com.toyota.demo.data.repository.ScanRepository(dao)
    }
}
