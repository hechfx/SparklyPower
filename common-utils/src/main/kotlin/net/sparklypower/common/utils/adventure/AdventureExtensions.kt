package net.sparklypower.common.utils.adventure

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

// Inspired by https://github.com/KyoriPowered/adventure/tree/main/4/extra-kotlin/src/main/kotlin/net/kyori/adventure/extra/kotlin
// But I tried to make it more ergonomic to use, because the original version is kinda... "bad" to use, because it doesn't
// really feel like a Kotlin DSL :(

// Instead of using "textComponent", we use "TextComponent", mostly because using "textComponent" feels a bit weird when using inside a DSL
fun TextComponent(block: TextComponent.Builder.() -> (Unit) = {}) = Component.text().apply(block).build()
fun TextComponent(content: String, block: TextComponent.Builder.() -> (Unit) = {}) = Component.text().content(content).apply(block).build()

fun TextComponent.Builder.append(color: TextColor, content: String, block: TextComponent.Builder.() -> (Unit) = {}) = append(
    Component.text()
        .color(color)
        .content(content)
        .apply(block)
)

fun TextComponent.Builder.append(content: String, block: TextComponent.Builder.() -> (Unit) = {}) = append(
    Component.text()
        .content(content)
        .apply(block)
)

fun TextComponent.Builder.appendTextComponent(block: TextComponent.Builder.() -> (Unit) = {}) = append(
    Component.text()
        .apply(block)
)

fun TextComponent.Builder.suggestCommandOnClick(command: String) {
    clickEvent(ClickEvent.clickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
}

fun TextComponent.Builder.runCommandOnClick(command: String) {
    clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command))
}

fun TextComponent.Builder.hoverText(block: TextComponent.Builder.() -> (Unit) = {}) {
    hoverEvent(HoverEvent.showText(TextComponent(block)))
}

fun TextComponent.Builder.appendCommand(command: String) = append(
    TextComponent {
        content(command)
        color(NamedTextColor.GOLD)
        suggestCommandOnClick(command)
        hoverText {
            content("Clique para executar o comando!")
        }
    }
)

// fun Audience.sendTextComponent(block: TextComponent.Builder.() -> (Unit) = {}) = sendMessage(TextComponent(block))