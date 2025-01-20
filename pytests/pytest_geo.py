import os
import shutil

# Define the folder paths and input file
input_file = r"D:\EntityJs\pytests\geo\wyrm.geo.json"
output_folder = r"D:\EntityJs\pytests\geo_out"

# Define the mapping of entity types to file names
entity_mapping = {
    "minecraft:zombie": "zombie.geo.json",
    "minecraft:allay": "allay.geo.json",
    "minecraft:axolotl": "axolotl.geo.json",
    "minecraft:bat": "bat.geo.json",
    "minecraft:bee": "bee.geo.json",
    "minecraft:blaze": "blaze.geo.json",
    "minecraft:boat": "boat.geo.json",
    "minecraft:camel": "camel.geo.json",
    "minecraft:cat": "cat.geo.json",
    "minecraft:chicken": "chicken.geo.json",
    "minecraft:cow": "cow.geo.json",
    "minecraft:creeper": "creeper.geo.json",
    "minecraft:dolphin": "dolphin.geo.json",
    "minecraft:donkey": "donkey.geo.json",
    "minecraft:enderman": "enderman.geo.json",
    "minecraft:evoker": "evoker.geo.json",
    "minecraft:ghast": "ghast.geo.json",
    "minecraft:goat": "goat.geo.json",
    "minecraft:guardian": "guardian.geo.json",
    "minecraft:horse": "horse.geo.json",
    "minecraft:illusioner": "illusioner.geo.json",
    "minecraft:iron_golem": "iron_golem.geo.json",
    "minecraft:panda": "panda.geo.json",
    "minecraft:parrot": "parrot.geo.json",
    "minecraft:eye_of_ender": "eye_of_ender.geo.json",
    "minecraft:piglin": "piglin.geo.json",
    "minecraft:wither": "wither.geo.json",
    "minecraft:slime": "slime.geo.json",
    "minecraft:skeleton": "skeleton.geo.json",
    "minecraft:wolf": "wolf.geo.json",
    "minecraft:trident": "trident.geo.json",
}

# Ensure the output folder exists
os.makedirs(output_folder, exist_ok=True)

# Copy and rename the file for each entity
for entity_type, file_name in entity_mapping.items():
    output_path = os.path.join(output_folder, file_name)
    try:
        shutil.copy(input_file, output_path)
        print(f"Created {output_path} for {entity_type}")
    except Exception as e:
        print(f"Failed to create {output_path} for {entity_type}: {e}")

print("File duplication complete.")
