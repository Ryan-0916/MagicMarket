java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    compileOnly("com.magicrealms:magiclib:${rootProject.properties["magic_lib_version"]}")
    compileOnly("org.apache.commons:commons-lang3:${rootProject.properties["apache_lang3_version"]}")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

