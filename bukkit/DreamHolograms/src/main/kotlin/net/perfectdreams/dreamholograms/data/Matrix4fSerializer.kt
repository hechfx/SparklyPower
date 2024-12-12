package net.perfectdreams.dreamholograms.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.FloatArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.joml.Matrix4f

object Matrix4fSerializer : KSerializer<Matrix4f> {
    private val FloatArraySerializer = FloatArraySerializer()
    override val descriptor: SerialDescriptor = SerialDescriptor("Matrix4f", FloatArraySerializer.descriptor)

    override fun serialize(encoder: Encoder, value: Matrix4f) {
        val array = FloatArray(16)
        value.get(array)
        encoder.encodeSerializableValue(FloatArraySerializer, array)
    }

    override fun deserialize(decoder: Decoder): Matrix4f {
        val array = decoder.decodeSerializableValue(FloatArraySerializer)
        require(array.size == 16) { "Matrix4f requires exactly 16 elements, got ${array.size}" }
        return Matrix4f().set(array)
    }

}