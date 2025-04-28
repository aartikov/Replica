package me.aartikov.replica.advanced_sample.app

import me.aartikov.replica.advanced_sample.core.coreModule
import me.aartikov.replica.advanced_sample.features.dudes.dudesModule
import me.aartikov.replica.advanced_sample.features.fruits.fruitsModule
import me.aartikov.replica.advanced_sample.features.pokemons.pokemonsModule
import me.aartikov.replica.advanced_sample.features.project.projectModule
import me.aartikov.replica.advanced_sample.features.search.searchModule

val allModules = listOf(
    coreModule,
    projectModule,
    pokemonsModule,
    fruitsModule,
    dudesModule,
    searchModule,
)