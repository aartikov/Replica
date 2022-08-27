package me.aartikov.replica.simple_sample.features.pokemons.data.dto

fun parseId(url: String): String {
    return url.split("/").dropLast(1).last()
}

fun formatName(name: String): String {
    return name.replace('-', ' ').replaceFirstChar { it.uppercase() }
}

fun decimetresToMeters(height: Int): Float {
    return height / 10.0f
}

fun hectogramsToKilograms(weight: Int): Float {
    return weight / 10.0f
}

fun getImageUrl(id: String): String {
    return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
}