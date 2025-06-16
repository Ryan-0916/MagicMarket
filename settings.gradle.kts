rootProject.name = "MagicMarket"
include(":common")
include(":api")
include(":core")

pluginManagement {
    repositories {
        gradlePluginPortal()
        /* PaperMC */
        maven("https://repo.papermc.io/repository/maven-public/")
        /* PlaceholderApi */
        maven("https://repo.extendedclip.com/releases/")
    }
}
include("api")
