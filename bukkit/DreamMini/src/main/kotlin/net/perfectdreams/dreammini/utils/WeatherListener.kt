package net.perfectdreams.dreammini.utils

import net.kyori.adventure.text.format.NamedTextColor
import net.perfectdreams.dreamcore.utils.adventure.appendCommand
import net.perfectdreams.dreamcore.utils.adventure.appendTextComponent
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import net.perfectdreams.dreammini.DreamMini
import org.bukkit.Bukkit
import org.bukkit.WeatherType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.weather.WeatherChangeEvent

class WeatherListener(val m: DreamMini) : Listener {
    @EventHandler
    fun onWeather(e: WeatherChangeEvent) {
        val playersInWorld = e.world.players

        for (player in playersInWorld) {
            val isRainDisabled = m.weatherBlacklist.contains(player.uniqueId)

            if (!isRainDisabled) {
                if (e.toWeatherState()) {
                    player.sendMessage(
                        textComponent {
                            color(NamedTextColor.YELLOW)
                            appendTextComponent {
                                content("A chuva começou... Se você odeia chuva, você pode desativá-la usando ")
                            }
                            appendCommand("/chuva")
                            appendTextComponent {
                                content("!")
                            }
                        }
                    )
                } else {
                    player.sendMessage(
                        textComponent {
                            color(NamedTextColor.YELLOW)
                            appendTextComponent {
                                content("A chuva parou! Se você odeia chuva e não quer mais chuviscos de novo, você pode desativá-la usando ")
                            }
                            appendCommand("/chuva")
                            appendTextComponent {
                                content("!")
                            }
                        }
                    )
                }
            } else {
                player.setPlayerWeather(WeatherType.CLEAR)

                if (e.toWeatherState()) {
                    player.sendMessage(
                        textComponent {
                            color(NamedTextColor.YELLOW)
                            appendTextComponent {
                                content("A chuva começou... Como você desativou a chuva, você não irá ver ela... Caso queria reativar a chuva, use ")
                            }
                            appendCommand("/chuva")
                            appendTextComponent {
                                content("!")
                            }
                        }
                    )
                } else {
                    player.sendMessage(
                        textComponent {
                            color(NamedTextColor.YELLOW)
                            appendTextComponent {
                                content("A chuva parou! Caso você está sentindo saudades de ver a chuva cair em vez de \"chover mas eu não consigo ver nada\", use ")
                            }
                            appendCommand("/chuva")
                            appendTextComponent {
                                content("!")
                            }
                        }
                    )
                }
            }
        }
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) { with (e.player) { if (uniqueId in m.weatherBlacklist) setPlayerWeather(WeatherType.CLEAR) } }
}