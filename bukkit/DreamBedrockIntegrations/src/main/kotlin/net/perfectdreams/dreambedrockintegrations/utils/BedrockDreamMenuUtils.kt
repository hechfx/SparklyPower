package net.perfectdreams.dreambedrockintegrations.utils

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.perfectdreams.dreambedrockintegrations.DreamBedrockIntegrations
import net.perfectdreams.dreamcore.utils.ClickContext
import net.perfectdreams.dreamcore.utils.DreamMenu
import net.perfectdreams.dreamcore.utils.KotlinPlugin
import net.perfectdreams.dreamcore.utils.scheduler.delayTicks
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.inventory.CraftInventoryView
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.geysermc.cumulus.component.ButtonComponent
import org.geysermc.cumulus.form.SimpleForm

object BedrockDreamMenuUtils {
    /**
     * Converts a [menu] into a Bedrock [SimpleForm]
     */
    fun convertDreamMenuToFormMenuAndSend(
        m: KotlinPlugin,
        menu: DreamMenu,
        player: Player
    ) {
        val dreamBedrockIntegrations = (Bukkit.getPluginManager().getPlugin("DreamBedrockIntegrations") as DreamBedrockIntegrations)

        val inventory = menu.createInventory() // This is not used by the form itself!!! We only use it to create a ClickContext

        val form = SimpleForm.builder()
            .title(LegacyComponentSerializer.legacySection().serialize(menu.title))

        val buttonWrappers = mutableListOf<DreamMenuButtonWrapper>()

        for (slot in menu.slots.sortedBy { it.position }) {
            val item = slot.item
            val onClick = slot.onClick

            if (item != null && onClick != null) {
                val buttonText = slot.item?.itemMeta?.displayName()?.let { LegacyComponentSerializer.legacySection().serialize(it) } ?: slot.item?.type?.name ?: "*desconhecido*"
                form.button(ButtonComponent.of(buttonText))
                buttonWrappers.add(DreamMenuButtonWrapper(onClick))
            }
        }

        form.validResultHandler { _, response ->
            val targetButton = buttonWrappers[response.clickedButtonId()]

            val clickCtx = ClickContext(menu, inventory)
            targetButton.onClick.invoke(clickCtx, player)
        }

        val openInventory = player.openInventory

        // This seems weird, but we need to do that because if we are moving from a GUI -> Form, the form does not show up if the inventory is not closed
        if (openInventory !is CraftInventoryView<*, *>) {
            player.closeInventory()
            m.launchMainThread {
                delayTicks(20L)
                dreamBedrockIntegrations.sendSimpleForm(player, form.build())
            }
        } else {
            dreamBedrockIntegrations.sendSimpleForm(player, form.build())
        }
    }

    private data class DreamMenuButtonWrapper(
        val onClick: ClickContext.(HumanEntity) -> (Unit)
    )
}