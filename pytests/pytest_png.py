import os
import shutil

# Define the input texture file and output folder
input_texture_file = r"D:\EntityJs\pytests\textures\wyrm.png"
output_texture_folder = r"D:\EntityJs\pytests\texture_out"

# Define the mapping of entity types to file names
entity_texture_mapping = {
    "minecraft:zombie": "zombie.png",
    "minecraft:allay": "allay.png",
    "minecraft:axolotl": "axolotl.png",
    "minecraft:bat": "bat.png",
    "minecraft:bee": "bee.png",
    "minecraft:blaze": "blaze.png",
    "minecraft:boat": "boat.png",
    "minecraft:camel": "camel.png",
    "minecraft:cat": "cat.png",
    "minecraft:chicken": "chicken.png",
    "minecraft:cow": "cow.png",
    "minecraft:creeper": "creeper.png",
    "minecraft:dolphin": "dolphin.png",
    "minecraft:donkey": "donkey.png",
    "minecraft:enderman": "enderman.png",
    "minecraft:evoker": "evoker.png",
    "minecraft:ghast": "ghast.png",
    "minecraft:goat": "goat.png",
    "minecraft:guardian": "guardian.png",
    "minecraft:horse": "horse.png",
    "minecraft:illusioner": "illusioner.png",
    "minecraft:iron_golem": "iron_golem.png",
    "minecraft:panda": "panda.png",
    "minecraft:parrot": "parrot.png",
    "minecraft:eye_of_ender": "eye_of_ender.png",
    "minecraft:piglin": "piglin.png",
    "minecraft:wither": "wither.png",
    "minecraft:slime": "slime.png",
    "minecraft:skeleton": "skeleton.png",
    "minecraft:wolf": "wolf.png",
    "minecraft:trident": "trident.png",
}

# Ensure the output folder exists
os.makedirs(output_texture_folder, exist_ok=True)

# Copy and rename the texture file for each entity
for entity_type, texture_name in entity_texture_mapping.items():
    output_path = os.path.join(output_texture_folder, texture_name)
    try:
        shutil.copy(input_texture_file, output_path)
        print(f"Created {output_path} for {entity_type}")
    except Exception as e:
        print(f"Failed to create {output_path} for {entity_type}: {e}")

print("Texture duplication complete.")
