package app.kehdo.data.user.di

import app.kehdo.data.user.BuildConfig
import app.kehdo.data.user.FakeUserRepository
import app.kehdo.data.user.RealUserRepository
import app.kehdo.domain.user.UserRepository
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt binding for [UserRepository]. Mirrors the [Fake/Real] selection
 * pattern in `:data:conversation/di/ConversationModule.kt` and reads
 * the same `kehdo.useFakeData` gradle property — both repositories
 * always switch together so the UI never sees a mismatched pair (Fake
 * conversations + Real usage, or vice versa).
 */
@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        fake: Lazy<FakeUserRepository>,
        real: Lazy<RealUserRepository>
    ): UserRepository =
        if (BuildConfig.USE_FAKE_DATA) fake.get() else real.get()
}
