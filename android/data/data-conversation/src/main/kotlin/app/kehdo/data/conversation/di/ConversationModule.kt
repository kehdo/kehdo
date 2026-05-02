package app.kehdo.data.conversation.di

import app.kehdo.data.conversation.BuildConfig
import app.kehdo.data.conversation.FakeConversationRepository
import app.kehdo.data.conversation.RealConversationRepository
import app.kehdo.domain.conversation.ConversationRepository
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt binding for [ConversationRepository].
 *
 * Selection is driven by the `kehdo.useFakeData` gradle property
 * (see `gradle.properties`):
 *
 * - **true** (default) → [FakeConversationRepository]. Canned in-memory
 *   data with simulated latency. Lets the Android flow be exercised
 *   end-to-end without needing the backend running locally.
 *
 * - **false** → [RealConversationRepository]. Talks to the kehdo
 *   backend at `BuildConfig.API_BASE_URL`. To use locally:
 *
 *   ```bash
 *   # Terminal 1: backend
 *   cd infra && docker compose up -d
 *   cd ../backend && ./gradlew :app:bootRun
 *
 *   # Terminal 2: Android
 *   cd android && ./gradlew :app:installDebug -Pkehdo.useFakeData=false
 *   ```
 *
 * Both implementations are constructed lazily, so the unused one's
 * collaborators (Retrofit clients on the Real path, in-memory state on
 * the Fake path) never spin up.
 */
@Module
@InstallIn(SingletonComponent::class)
object ConversationModule {

    @Provides
    @Singleton
    fun provideConversationRepository(
        fake: Lazy<FakeConversationRepository>,
        real: Lazy<RealConversationRepository>
    ): ConversationRepository =
        if (BuildConfig.USE_FAKE_DATA) fake.get() else real.get()
}
