package no.nav.soknad.arkivering.soknadsmottaker.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.jwt.JwtClaimValidator
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.Base64


@Configuration
class SecurityConfig(
	@Value("\${auth.issuers.azuread.issuer-uri}") private val azureadIssuer: String,
	@Value("\${auth.issuers.azuread.jwk-set-uri}") private val azureadJwkUri: String,
	@Value("\${auth.issuers.azuread.accepted-audience}") private val azureadAudience: String
){
	private val logger = LoggerFactory.getLogger(this::class.java)

	@Bean
	fun azureJwtDecoder(): JwtDecoder {
		val nimbusJwtDecoder = NimbusJwtDecoder.withJwkSetUri(azureadJwkUri).build()

		// Oppretter en validator for 'aud' claimet
		val audienceValidator = JwtClaimValidator<List<String>>(JwtClaimNames.AUD) { aud ->
			aud.contains(azureadAudience)
		}

		// Kombinerer standard validatorer (som 'iss' og 'exp') med vår audience-validator
		val withIssuer = JwtValidators.createDefaultWithIssuer(azureadIssuer)
		val withAudience = DelegatingOAuth2TokenValidator(withIssuer, audienceValidator)

		nimbusJwtDecoder.setJwtValidator(withAudience)
		return nimbusJwtDecoder
	}

	// JSON mapper from Jackson 3 (tools.jackson)
	private val mapper = jacksonObjectMapper()

	/**
	 * Light-weight issuer extraction: parse token payload (no crypto/validation) to read "iss".
	 * Returns null if it cannot parse.
	 */
	private fun extractIssuer(tokenValue: String): String? {
		val parts = tokenValue.split(".")
		if (parts.size < 2) return null
		return try {
			val payloadPart = Base64.getUrlDecoder().decode(parts[1])
			val node = mapper.readTree(payloadPart)
			node.get("iss")?.asText()
		} catch (ex: Exception) {
			logger.warn("Klarte ikke å parse issuer fra token", ex)
			null
		}
	}

	@Bean
	fun securityFilterChain(
		http: HttpSecurity,
		azureJwtDecoder: JwtDecoder,
	): SecurityFilterChain {

		val delegatingDecoder = JwtDecoder { token ->
			val iss = extractIssuer(token)
				?: throw BadCredentialsException("Manglende eller ugyldig issuer i token")

			when (iss) {
				azureadIssuer -> azureJwtDecoder.decode(token)
				else -> {
					logger.info("Ukjent issuer: $iss")
					throw BadCredentialsException("Ukjent issuer: $iss")
				}
			}
		}
		val jwtAuthConverter = JwtAuthenticationConverter().apply {
			setJwtGrantedAuthoritiesConverter(JwtGrantedAuthoritiesConverter())
		}

		http
			.csrf { csrf -> csrf.disable() }
			//.csrf { csrf ->
			//	csrf.ignoringRequestMatchers("/isAlive", "/isReady", "/health/**", "/public/**")
			//}
			.authorizeHttpRequests { auth ->
				// Authorize all HTTP requests
				auth.requestMatchers("/isAlive", "/isReady", "/health/**", "/public/**").permitAll()
				auth.anyRequest().authenticated()
			}
			.oauth2ResourceServer { rs ->
				rs.jwt { jwt ->
					jwt.decoder(delegatingDecoder)
					jwt.jwtAuthenticationConverter(jwtAuthConverter)
				}
			}
			return http.build()
	}

}
