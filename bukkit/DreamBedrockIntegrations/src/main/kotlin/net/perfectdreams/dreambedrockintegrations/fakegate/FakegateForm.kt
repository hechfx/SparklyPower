package net.perfectdreams.dreambedrockintegrations.fakegate

import com.google.common.base.Charsets
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap
import net.perfectdreams.dreambedrockintegrations.DreamBedrockIntegrations
import org.bukkit.entity.Player
import org.geysermc.cumulus.form.Form
import org.geysermc.cumulus.form.impl.FormDefinitions
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level

// This is Floodgate's "FormChannel" code, but a bit modified
// This was changed to be "per-player", because originally Floodgate uses a global instance, and that causes issues with memory leaks because
// form instances are never cleared!!
class FakegateForm(val m: DreamBedrockIntegrations) {
    companion object {
        fun getIdentifier(): String {
            return "floodgate:form"
        }

        private val formDefinitions: FormDefinitions = FormDefinitions.instance()
    }

    val storedForms: Short2ObjectMap<Form> = Short2ObjectMaps.synchronize(Short2ObjectOpenHashMap())
    private val nextFormId = AtomicInteger(0)

    fun handleServerCall(data: ByteArray, playerUuid: UUID?, playerUsername: String) {
        callResponseConsumer(data)
    }

    fun sendForm(player: Player, form: Form): Boolean {
        val formData = createFormData(form)
        player.sendPluginMessage(m, getIdentifier(), formData)
        return true
    }

    fun createFormData(form: Form): ByteArray {
        val formId = getNextFormId()
        // We are NEVER going to be the proxy
        // if (config.isProxy()) {
        //     formId = (formId.toInt() or 0x8000).toShort()
        // }
        storedForms.put(formId, form)

        val definition = formDefinitions.definitionFor(form)

        val jsonData =
            definition.codec()
                .jsonData(form)
                .toByteArray(Charsets.UTF_8)

        val data = ByteArray(jsonData.size + 3)
        data[0] = definition.formType().ordinal.toByte()
        data[1] = (formId.toInt() shr 8 and 0xFF).toByte()
        data[2] = (formId.toInt() and 0xFF).toByte()
        System.arraycopy(jsonData, 0, data, 3, jsonData.size)
        return data
    }

    private fun callResponseConsumer(data: ByteArray): Boolean {
        val storedForm = storedForms.remove(getFormId(data))
        if (storedForm != null) {
            val responseData = String(data, 2, data.size - 2, Charsets.UTF_8)
            try {
                formDefinitions.definitionFor(storedForm)
                    .handleFormResponse(storedForm, responseData)
            } catch (e: Exception) {
                m.logger.log(Level.SEVERE, "Error while processing form response!", e)
            }
            return true
        }
        return false
    }

    private fun getFormId(data: ByteArray): Short {
        return ((data[0].toInt() and 0xFF) shl 8 or (data[1].toInt() and 0xFF)).toShort()
    }

    protected fun getNextFormId(): Short {
        // signed bit is used to check if the form is from a proxy or a server
        return nextFormId.getAndUpdate { number: Int -> if (number == Short.MAX_VALUE.toInt()) 0 else number + 1 }
            .toShort()
    }
}