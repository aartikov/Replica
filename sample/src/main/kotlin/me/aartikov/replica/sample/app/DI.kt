package me.aartikov.replica.sample.app

import me.aartikov.replica.sample.core.coreModule
import me.aartikov.replica.sample.features.fruits.fruitsModule
import me.aartikov.replica.sample.features.pokemons.pokemonsModule
import me.aartikov.replica.sample.features.project.projectModule

val allModules = listOf(
    coreModule,
    projectModule,
    pokemonsModule,
    fruitsModule
)