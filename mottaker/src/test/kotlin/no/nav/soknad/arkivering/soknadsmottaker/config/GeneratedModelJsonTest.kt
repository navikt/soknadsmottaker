package no.nav.soknad.arkivering.soknadsmottaker.config

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.soknad.arkivering.soknadsmottaker.model.ApplicationStatus
import no.nav.soknad.arkivering.soknadsmottaker.model.ApplicationStatusType
import no.nav.soknad.arkivering.soknadsmottaker.model.SoknadRef
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

class GeneratedModelJsonTest {

	private val mapper = JacksonConfig().objectMapper()
	private val referenceJson = """
		{
			"innsendingId": "test-submission",
			"erEttersendelse": false,
			"groupId": "test-group",
			"personId": "test-person",
			"tidpunktEndret": "2026-01-02T03:04:05+01:00"
		}
	""".trimIndent()

	@Test
	fun `omits absent optional fields from status responses`() {
		val status = ApplicationStatus(ApplicationStatusType.OK)

		assertEquals(
			mapper.readTree("""{"status":"OK"}"""),
			mapper.readTree(mapper.writeValueAsString(status))
		)
	}

	@Test
	fun `keeps the default flag when an optional field is explicitly null`() {
		val input = mapper.readValue(referenceJson, ObjectNode::class.java)
		input.putNull("erSystemGenerert")

		val reference = mapper.treeToValue(input, SoknadRef::class.java)

		assertEquals(false, reference.erSystemGenerert)
	}

	@Test
	fun `preserves the date offset and false flags in JSON roundtrips`() {
		val reference = mapper.readValue(referenceJson, SoknadRef::class.java)
		val serialized = mapper.readTree(mapper.writeValueAsString(reference))

		assertEquals(OffsetDateTime.parse("2026-01-02T03:04:05+01:00"), reference.tidpunktEndret)
		assertEquals("2026-01-02T03:04:05+01:00", serialized["tidpunktEndret"].asText())
		assertEquals(mapper.readTree("false"), serialized["erEttersendelse"])
		assertEquals(mapper.readTree("false"), serialized["erSystemGenerert"])
	}

	@Test
	fun `rejects a missing required status`() {
		assertThrows(JsonMappingException::class.java) {
			mapper.readValue("{}", ApplicationStatus::class.java)
		}
	}

	@Test
	fun `rejects an unknown status value`() {
		assertThrows(JsonMappingException::class.java) {
			mapper.readValue("""{"status":"UNKNOWN"}""", ApplicationStatus::class.java)
		}
	}
}
