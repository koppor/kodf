plugins {
    application
    id("com.diffplug.spotless") version "8.12.0"
}

group = "io.github.koppor"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(27)
    }
}

dependencies {
    implementation("info.picocli:picocli:4.7.7")
    implementation("org.xerial:sqlite-jdbc:3.54.1.0")

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "io.github.koppor.kodf.App"
    // sqlite-jdbc loads its bundled native library
    applicationDefaultJvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

spotless {
    java {
        googleJavaFormat("2.0.0")
    }
}
