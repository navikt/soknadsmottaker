package no.nav.soknad.arkivering.soknadsmottaker.config

import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DatumWriter
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer
import java.io.ByteArrayOutputStream
import java.io.IOException

class AvroSerializer<T : SpecificRecordBase> : Serializer<T> {
	override fun close() {}

	override fun configure(arg0: Map<String?, *>?, arg1: Boolean) {}

	override fun serialize(topic: String, data: T?): ByteArray? {
		return try {
			var result: ByteArray? = null
			if (data != null) {

				val byteArrayOutputStream = ByteArrayOutputStream()
				byteArrayOutputStream.use {

					val binaryEncoder = EncoderFactory.get().binaryEncoder(byteArrayOutputStream, null)
					val datumWriter: DatumWriter<GenericRecord> = GenericDatumWriter(data.schema)

					datumWriter.write(data, binaryEncoder)
					binaryEncoder.flush()

					result = byteArrayOutputStream.toByteArray()
				}
			}
			result
		} catch (ex: IOException) {
			throw SerializationException("Can't serialize data='$data' for topic='$topic'", ex)
		}
	}
}
