package app.kehdo.data.auth.di

import app.kehdo.data.auth.StubAuthRepository
import app.kehdo.domain.auth.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: StubAuthRepository): AuthRepository
}
