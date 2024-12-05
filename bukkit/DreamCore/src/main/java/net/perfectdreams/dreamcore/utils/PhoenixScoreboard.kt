package net.perfectdreams.dreamcore.utils

import com.google.common.base.Splitter
import io.papermc.paper.scoreboard.numbers.NumberFormat
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.perfectdreams.dreamcore.utils.adventure.textComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard

/**
 * PhoenixScoreboard - A scoreboard with flickers
 *
 * Originally it used teams for no flicker, but nowadays, we use scores directly because newer Minecraft versions are waaaay better :3
 *
 * @author MrPowerGamerBR
 */
class PhoenixScoreboard(val scoreboard: Scoreboard) {
	/**
	 * Used to avoid getting and setting the score in the scoreboard multiple times for no reason
	 */
	private val lineVisibility = mutableMapOf<Int, Boolean>()

	init {
		val objective = scoreboard.registerNewObjective("alphys", Criteria.DUMMY, null) // the display name must be null, if not 1.20.3 are able to see the objective
		objective.displaySlot = DisplaySlot.SIDEBAR
	}

	fun setText(text: String, line: Int) {
		setText(
			LegacyComponentSerializer.legacyAmpersand().deserialize(text),
			line
		)
	}

	fun setText(text: TextComponent, line: Int) {
		val alphys = scoreboard.getObjective("alphys")!!
		val currentScore = alphys.getScore("line$line")

		currentScore.score = line
		currentScore.customName(text)
		currentScore.numberFormat(NumberFormat.blank())
	}

	fun removeLine(line: Int) {
		scoreboard.resetScores("line$line")
		lineVisibility[line] = false
	}

	fun setTitle(title: String) {
		scoreboard.getObjective("alphys")!!.displayName = title
	}

	fun setTitle(title: TextComponent) {
		scoreboard.getObjective("alphys")!!.displayName(title)
	}
}

