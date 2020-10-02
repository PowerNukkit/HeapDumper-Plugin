# HeapDumper Pluging
This plugin adds the /heapdump command to your server, it requires the permission `heapdump` which is given
to OP players by default.

The command takes a memory heap dump and saves it as a file in the server's directory. The file name uses the
current system time by default, but you can customize it by command argument.

Be aware that the heap dump files are usually very big since it will dump the entire application memory in use in a single
file, this means that if your server is using 4GB of RAM when the command is executed, the file will also take 4GB 
in the disk space.

Also be aware that heap dump files may contains sensitive information, like database connection information and
unencrypted passwords, so never leave the dumps there.

## Commands and Permissions

| Command          | Permission                    | Usage                           | Description
|------------------|-------------------------------|---------------------------------|------------------
| `/heapdump`      | `heapdumper.heapdump`         | `/heapdump <optional-filename>` | Take a heap dump to analyze memory leak issues and save in the server's folder
| `/cleardump`     | `heapdumper.cleardump.single` | `/cleardump <filename>`         | Deletes a specific heap dump file
| `/clearalldumps` | `heapdumper.cleardump.all`    | `/clearalldumps`                | Deletes all heap dump files at once 

## Glob Permissions
- `heapdumper.cleardump.*` - Allows the user to run the command that clears the dumps
  - `heapdumper.cleardump.single`
  - `heapdumper.cleardump.all`
- `heapdumper.*` - Allows the user to run all the heapdumper commands
  - `heapdumper.heapdump`
  - `heapdumper.cleardump.*`

## Cloning and importing
1. Just do a normal `git clone https://github.com/PowerNukkit/HeapDumper-Plugin.git` (or the URL of your own git repository)
2. Import the `pom.xml` file with your IDE, it should do the rest by itself

## Debugging
1. Create a zip file containing only your `plugin.yml` file
2. Rename the zip file to change the extension to jar
3. Create an empty folder anywhere, that will be your server folder.  
   <small>_Note: You don't need to place the PowerNukkit jar in the folder, your IDE will load it from the maven classpath._</small>
4. Create a folder named `plugins` inside your server folder  
   <small>_Note: It is needed to bootstrap your plugin, your IDE will load your plugin classes from the classpath automatically,
   so it needs to have only the `plugin.yml` file._</small>
5. Move the jar file that contains only the `plugin.yml` to the `plugins` folder
6. Create a new Application run configuration setting the working directory to the server folder and the main class to:  `cn.nukkit.Nukkit`  
![](https://i.imgur.com/NUrrZab.png)
7. Now you can run in debug mode. If you change the `plugin.yml` you will need to update the jar file that you've made.
