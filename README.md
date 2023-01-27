# MCPlayerTransfer
A tool for replace player's data with a new account.

## Introduction
Have you been annoying when losing all the advancements, statistics, and even, the most horrible thing, that the ownership of your pets?
This will happen when you enter your old world with a new account of Minecraft. To prevent it, this tool may help you.


## How to use
After compile codes into jar file(assume it as mcpt.jar), type the following command on your console:
```shell
java -jar mcpt.jar -t <target_uuid> -r <replacement_uuid> -s <save_folder> -o <output_folder>
```
Let me explain each argument's meaning:

`UUID`: unique id of player

You could leave a path to level.dat or /path/to/save/playerdata/<player_uuid>.dat,
and the program will automatically find the uuid.

You can also directly paste a player uuid, which could be found at https://mcuuid.net/.

`save_folder`: Path to your world's save folder. They usually located in ./minecraft/saves/YOUR_WORLD_NAME

`output_folder`: The ROOT FOLDER where the translated folder exist. The usually be your new 'saves' folder.

Example: 
```shell
java mpt.jar -t saves/MyWorld/level.dat -r YOUR_NEW_UUID -s saves/MyWorld -o new_saves
```

## How does it work?
The program will scan the mc save folder and find a couple of crucial files, which contains UUID field.
The files/folders is:
1. level.dat and level.dat_old
2. advancements/<uuid>.json
3. stats/<uuid>.json
4. entities/*.mca
5. DIM1/entities/*.mca (entities in The End, the world dimension is 1)
6. DIM-1/entities/*.mca (entities in The Nether, the world dimension is -1)
7. playerdata/<uuid>.dat and playerdata/<uuid>.dat_old

After identified those files, the program will replace all the field that contains old player's uuid with new one.

## Thanks
https://minecraft.fandom.com/wiki/Java_Edition_level_format

https://github.com/Querz/NBT
