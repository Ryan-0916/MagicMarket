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
        /* Luckperms */
        maven("https://repo1.maven.org/maven2/net/luckperms/api/")
    }
}
include("api")
