package net.perfectdreams.dreamholograms.commands

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.adventure.append
import net.perfectdreams.dreamcore.utils.commands.context.CommandContext
import net.perfectdreams.dreamcore.utils.commands.declarations.SparklyCommandDeclarationWrapper
import net.perfectdreams.dreamcore.utils.commands.declarations.sparklyCommand
import net.perfectdreams.dreamholograms.DreamHolograms
import net.perfectdreams.dreamholograms.data.HologramLine

class DreamHologramsCommand(val m: DreamHolograms) : SparklyCommandDeclarationWrapper {
    companion object {
        inline fun <reified T : HologramLine> sendInvalidDisplayType(context: CommandContext) {
            context.sendMessage {
                color(NamedTextColor.RED)
                when (T::class) {
                    HologramLine.HologramText::class -> content("Linha não é um text display!")
                    HologramLine.HologramItem::class -> content("Linha não é um item drop!")
                    else -> error("Unknown display type!")
                }
            }
        }

        fun sendHologramDoesNotExist(context: CommandContext, hologramName: String) {
            context.sendMessage {
                color(NamedTextColor.RED)
                append("Holograma ")
                append(hologramName) {
                    color(NamedTextColor.AQUA)
                }
                append(" não existe!")
            }
        }

        fun sendInvalidLine(context: CommandContext) {
            context.sendMessage {
                color(NamedTextColor.RED)
                content("Linha inválida!")
            }
        }

        fun sendLineDoesNotExist(context: CommandContext) {
            context.sendMessage {
                color(NamedTextColor.RED)
                content("Linha não existe!")
            }
        }
    }

    // The hologram and hd aliases are mostly due to "old habits die hard" (HolographicDisplays)
    override fun declaration() = sparklyCommand(listOf("dreamholograms", "hologram", "hd", "holo", "holograms")) {
        permission = "dreamholograms.manage"

        executor = DreamHologramsHelpExecutor(m)

        subcommand(listOf("create")) {
            executor = DreamHologramsCreateTextExecutor(m)
        }

        subcommand(listOf("movehere")) {
            executor = DreamHologramsMoveHereExecutor(m)
        }

        subcommand(listOf("lookatme")) {
            executor = DreamHologramsLookAtMeExecutor(m)
        }

        subcommand(listOf("delete")) {
            executor = DreamHologramsDeleteExecutor(m)
        }

        subcommand(listOf("relmove")) {
            executor = DreamHologramsRelativeMovementExecutor(m)
        }

        subcommand(listOf("near")) {
            executor = DreamHologramsNearExecutor(m)
        }

        subcommand(listOf("align")) {
            executor = DreamHologramsAlignExecutor(m)
        }

        subcommand(listOf("center")) {
            executor = DreamHologramsCenterExecutor(m)
        }

        subcommand(listOf("teleport")) {
            executor = DreamHologramsTeleportExecutor(m)
        }

        subcommand(listOf("save")) {
            executor = DreamHologramsSaveExecutor(m)
        }

        subcommand(listOf("reload")) {
            executor = DreamHologramsReloadExecutor(m)
        }

        subcommand(listOf("lines")) {
            subcommand(listOf("add")) {
                subcommand(listOf("text")) {
                    executor = DreamHologramsAddTextExecutor(m)
                }

                subcommand(listOf("item")) {
                    executor = DreamHologramsAddItemStackExecutor(m)
                }
            }

            subcommand(listOf("edit")) {
                subcommand(listOf("text")) {
                    subcommand(listOf("content")) {
                        executor = DreamHologramsSetTextContentExecutor(m)
                    }

                    subcommand(listOf("billboard")) {
                        executor = DreamHologramsBillboardExecutor(m)
                    }

                    subcommand(listOf("background")) {
                        subcommand(listOf("disable")) {
                            executor = DreamHologramsBackgroundDisableExecutor(m)
                        }

                        subcommand(listOf("set")) {
                            executor = DreamHologramsBackgroundColorExecutor(m)
                        }

                        subcommand(listOf("reset")) {
                            executor = DreamHologramsBackgroundColorExecutor(m)
                        }
                    }

                    subcommand(listOf("textshadow")) {
                        executor = DreamHologramsTextShadowExecutor(m)
                    }

                    subcommand(listOf("brightness")) {
                        subcommand(listOf("fullbright")) {
                            executor = DreamHologramsFullBrightnessExecutor(m)
                        }

                        subcommand(listOf("set")) {
                            executor = DreamHologramsBrightnessExecutor(m)
                        }

                        subcommand(listOf("reset")) {
                            executor = DreamHologramsResetBrightnessExecutor(m)
                        }
                    }

                    subcommand(listOf("transformation")) {
                        subcommand(listOf("scale")) {
                            executor = DreamHologramsTransformationScaleExecutor(m)
                        }

                        subcommand(listOf("reset")) {
                            executor = DreamHologramsTransformationResetExecutor(m)
                        }
                    }
                }

                subcommand(listOf("item")) {
                    subcommand(listOf("itemstack")) {
                        executor = DreamHologramsSetItemStackExecutor(m)
                    }
                }
            }

            subcommand(listOf("remove")) {
                executor = DreamHologramsRemoveLineExecutor(m)
            }
        }
    }
}