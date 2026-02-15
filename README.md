<br />
<h1 align="center">
  Kaisa's Extra Biome Generators
</h1>

This mod adds a new biome source type to Minecraft: `extrabiomegen:multinoise_discrete`.
This biome source type allows defining biome generation by using discrete groups for each
parameter (depth, continentalness, erosion, humidity, temperature, and weirdness). This
can be authored with a [visual tool](https://kaisadilla.github.io/extrabiomegen-composer/)
instead of having to write the json file manually, which vastly simplifies the process.
This biome source type also introduces a new parameter: region. This parameter allows defining
multiple biomes for a single group without having to squeeze it between existing biomes, which helps
creating biome sources with hundreds of biomes that do not compete with each other any more than
they do in Vanilla Minecraft. Additionally, this mod also allows altering biome placement
with Voronoi noise, which decouples biomes from terrain slightly, removing minuscule biomes that
often appear in Vanilla Minecraft's generation as well as breaking visible boundaries that
naturally emerge from noise maps.

With the sample data file provided in the visual tool, surface, river, ocean and exotic island
biome placement is almost identical to vanilla, while cave biome placement is similar yet not
identical. This is by design, as groups have been defined by using the same values as vanilla's
overworld biome placement.

This mod is intended for modpackers to easily customize biome generation in a simple, visual
and effective way.

This mod is developed for Forge for Minecraft 1.20.1, and it's in an Alpha stage, which means
it is not yet suitable for non-experimental use.
