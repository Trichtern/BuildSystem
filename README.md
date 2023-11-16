![Screenshot](.github/images/header.png)

# BuildSystem ![GitHub Workflow Status (branch)](https://img.shields.io/github/actions/workflow/status/thomasmny/BuildSystem/build.yml?branch=master) ![version](https://img.shields.io/github/v/release/thomasmny/BuildSystem) [![Discord](https://img.shields.io/discord/419460301403193344.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.com/rduPF3yk62)

## Table of contents

* [Introduction](#introduction)
* [Links and Contacts](#links-and-contacts)
* [Features](#features)
* [Screenshots](#screenshots)
* [Statistics](#statistics)
* [Contributing](#contributing)
    * [To compile...](#to-compile)
    * [Then you will find...](#then-you-will-find)
    * [Other commands](#other-commands)
    * [PR Policy](#pr-policy)
* [License](#license)

## Introduction

**BuildSystem** is a simple but powerful - as the name already says - system for builders, with lots of great features
for everyday usage. Manage worlds in the worlds navigator, change their permission, projects and status with ease. And
not to forget: let each player decide which settings whey think are best for them and now the building can start!

## Links and Contacts

* **Full guide:**
  You can find a full guide with tutorials, commands and permissions [here](https://eintosti.gitbook.io/buildsystem/).
* **Downloads:**
    * [SpigotMC](https://spigotmc.org/resources/buildsystem-1-8-1-18.60441/)
    * [Chunkfactory](https://chunkfactory.com/product/buildsystem-1-8-1-15.1049/)
    * [MC-Market](https://www.mc-market.org/resources/12399/)
    * Snapshots are available on [Jenkins](https://ci.eintosti.de/job/BuildSystem/).
* **Support:**
    * [GitHub issue tracker](https://github.com/einTosti/BuildSystem/issues)
    * [Discord](https://discord.gg/rduPF3yk62)
* **Donations:**
  Donations are **100%** voluntary. However, I am truly grateful for every single one of you who decides to do so as
  each donation helps me to continue developing the project further. If you wish to donate anything, you can do
  so [here](https://einTosti.com/donate).

## Features

* **100% customisable messages** and scoreboard
* Powerful navigator which allows for an overview of all worlds. Extra GUIs for:
    - **_Not Started_**, **_In Progress_**, **_Almost Finished_** and **_Finished_** maps
    - Maps that have been put to the **_Archive_**
    - **_Private_** player maps: Each player can create their own map, if a map with their name doesn't exist
* **Create worlds with ease**: When creating a world, choose from:
    - **_Predefined worlds_** _or_
    - **_Custom generators_** provided by 3rd party plugins _or_
    - **_Custom templates_** which you can add yourself
* Easily **manage your worlds**: Choose from over 6 different statuses for each world
* When in worlds that are set as finished, the player is invisible and can fly in adventure mode, so they can only have
  a look without breaking anything [bypass: `/build`]
* Set the **permission** you need to join a worlds
* Change what project each world is for (e.g. `"A small BedWars map"`)
* Set the **world item**, so you can spot them faster amongst other worlds
    - You can choose from all items that are available in Minecraft
* **Import**, **delete** and **create worlds** with ease
* New and improved navigator!
    - Not a GUI anymore, but furthermore an interactive navigator
    - But if you rather have a "normal" GUI, the option is there for you to toggle between the
* **Per player settings**
* **Building tools**:
    - Toggle block physics
    - Custom speed
    - Receive player skulls
    - Change the time in a world with only a single command
    - Toggle double slab breaking
    - No-Clip
    - Open iron doors and iron trapdoors
    - Night vision
    - Disable interactions with certain blocks

## Screenshots

### Navigator

![Screenshot](.github/images/navigator.png)

### World Navigator

![Screenshot](.github/images/worlds.png)

### Create predefined worlds

![Screenshot](.github/images/predefined_worlds.png)

### Create worlds with the help of custom templates

![Screenshot](.github/images/templates.png)

### Receive secret blocks

![Screenshot](.github/images/blocks.png)

### Change which status each world has

![Screenshot](.github/images/status.png)

### Per player settings

![Screenshot](.github/images/settings.png)

### Change the default items

![Screenshot](.github/images/setup.png)

### Custom scoreboard

![Screenshot](.github/images/scoreboard.png)

## Statistics

![Graph](https://bstats.org/signatures/bukkit/BuildSystem.svg)

## Developer API

**Maven:**

```xml

<repository>
  <id>eintosti-releases</id>
  <url>https://repo.eintosti.de/releases</url>
</repository>
```

```xml

<dependency>
  <groupId>de.eintosti</groupId>
  <artifactId>buildsystem-api</artifactId>
  <version>version</version>
</dependency>
 ```

**Or alternatively, with Gradle:**

```kotlin
repositories {
  maven {
    url = uri("https://repo.eintosti.de/releases")
  }
}

dependencies {
  compileOnly("de.eintosti:buildsystem-api:version")
}
```

## Contributing

### To compile...

#### On Windows

1. Shift + right-click the folder with the directory’s files and click "Open command prompt".
2. `gradlew clean build`

#### On Linux, BSD, or Mac OS X

1. In your terminal, navigate to the folder with directory’s files (cd /folder/of/buildsystem/files)
2. `./gradlew clean build`

### Then you will find...

* the **BuildSystem** plugin jar `BuildSystem-<identifier>` in **buildsystem-core/build/libs**

### Other commands

* `gradlew idea` will generate an [IntelliJ IDEA](https://www.jetbrains.com/idea/) module for each folder.
* `gradlew eclipse` will generate an [Eclipse](https://www.eclipse.org/downloads/) project for each folder. _(Possibly
  broken!)_

### PR Policy

I'll accept changes that make sense. You should be able to justify their existence, along with any maintenance costs
that come with them.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE.txt).