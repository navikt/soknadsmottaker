package no.nav.soknad.arkivering.soknadsmottaker.config

//import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.Base64


@Configuration
//@Profile("dev | prod")
//@EnableJwtTokenValidation
//@EnableWebSecurity
class SecurityConfig(
	@Value("\${auth.issuers.azuread.issuer-uri}") private val azureadIssuer: String,
	@Value("\${auth.issuers.azuread.jwk-set-uri}") private val azureadJwkUri: String,
){
	private val logger = LoggerFactory.getLogger(this::class.java)

	@Bean
	fun azureJwtDecoder(): JwtDecoder =
		NimbusJwtDecoder.withJwkSetUri(azureadJwkUri).build()

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
			// KORRIGERING: Bruk throw i stedet for lambda-blokk
			val iss = extractIssuer(token)
				?: throw BadCredentialsException("Klarte ikke å utlede issuer fra token")

			// KORRIGERING: Logg hva vi faktisk sammenligner for enklere feilsøking
			logger.debug("Dekoder token med issuer: $iss. Forventet azure issuer: $azureadIssuer")

			when (iss) {
				azureadIssuer -> azureJwtDecoder.decode(token)
				else -> throw BadCredentialsException("Ukjent issuer: $iss")
			}
		}

		val jwtAuthConverter = JwtAuthenticationConverter().apply {
			setJwtGrantedAuthoritiesConverter(JwtGrantedAuthoritiesConverter())
		}

		http
			.csrf { csrf -> csrf.disable() }
			.authorizeHttpRequests { auth ->
			// Authorize all HTTP requests
				auth.requestMatchers("/isAlive", "/isReady", "/health/**", "/public/**").permitAll()
				auth.anyRequest().authenticated()
			}
			.oauth2ResourceServer { rs ->
				rs.jwt { jwt ->
					// register our delegating decoder
					jwt.decoder(delegatingDecoder)
					jwt.jwtAuthenticationConverter(jwtAuthConverter)
				}
			}
			return http.build()
	}

}
