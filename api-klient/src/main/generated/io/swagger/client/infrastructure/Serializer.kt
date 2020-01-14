package io.swagger.client.infrastructure

import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import java.util.*

import no.nav.soknad.arkivering.dto.SoknadInnsendtDto

object Serializer {
    @JvmStatic
    val moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
						.add(SoknadInnsendtDto::class.java)
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .build()
}
