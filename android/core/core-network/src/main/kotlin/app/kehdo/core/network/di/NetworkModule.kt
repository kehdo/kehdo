package app.kehdo.core.network.di

import app.kehdo.core.network.BuildConfig
import app.kehdo.core.network.api.AuthApi
import app.kehdo.core.network.api.ConversationApi
import app.kehdo.core.network.auth.AuthInterceptor
import app.kehdo.core.network.auth.TokenRefreshAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/** OkHttp client with NO Authorization injected. Use it for talking to
 *  presigned URLs (S3) where adding a Bearer header breaks the signature. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UnauthenticatedHttpClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        authenticator: TokenRefreshAuthenticator
    ): OkHttpClient {
        // /v1/conversations/{id}/generate is the slowest endpoint by an
        // order of magnitude — Cloud Vision OCR (~3-5s) + Vertex AI Gemini /
        // OpenAI failover (~5-15s) + OpenAI moderation (~1-2s), all serial,
        // can take 30+ seconds on a warm path and 60+ on cold start. The
        // backend's own circuit breakers cap each external hop, so a 90s
        // client read budget bounds the worst-case round trip while still
        // giving the pipeline room to breathe. /refine has the same shape.
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .authenticator(authenticator)

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
        }
        // TODO(security): add CertificatePinner once production cert SHA-256 is finalized.
        // Pinning is intentionally absent in debug so local backend (HTTP) and self-signed
        // staging certs work — release pinning will be wired here for `api.kehdo.app`.
        return builder.build()
    }

    @Provides
    @Singleton
    @UnauthenticatedHttpClient
    fun provideUnauthenticatedOkHttpClient(): OkHttpClient {
        // S3 presigned URLs come with auth baked into query params — adding
        // an Authorization header (or wrapping in our refresh authenticator)
        // would rewrite the request and invalidate the signature. Plain
        // bytes-and-content-length only.
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            // Uploads are bytes-out, not bytes-in, so the read timeout
            // governs the server's 200 OK; keep it generous for slow
            // mobile networks.
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideConversationApi(retrofit: Retrofit): ConversationApi =
        retrofit.create(ConversationApi::class.java)
}
