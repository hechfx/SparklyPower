package net.sparklypower.sparklyvelocitycore.utils.commands.declarations

import net.sparklypower.sparklyvelocitycore.utils.commands.executors.SparklyCommandExecutor

open class SparklyCommandDeclaration(
    val labels: List<String>,
    val permissions: List<String>? = null,
    val executor: SparklyCommandExecutor? = null,
    val subcommands: List<SparklyCommandDeclaration>,
    val childrenInheritPermissions: Boolean
)