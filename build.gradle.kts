plugins {
    application
    id("com.diffplug.spotless") version "8.12.0"
}

group = "io.github.koppor"

// The JBang header of App.java declares the Java version and the dependencies, so that
// `jbang kodf@koppor/kodf` and this build cannot drift apart.
val jbangHeader = file("src/main/java/io/github/koppor/kodf/App.java").readLines()
    .filter { it.startsWith("//") }

java {
    toolchain {
        // [impl->adr~java~1]
        languageVersion = JavaLanguageVersion.of(
            jbangHeader.first { it.startsWith("//JAVA ") }.removePrefix("//JAVA ").trim().toInt(),
        )
    }
}

dependencies {
    jbangHeader.filter { it.startsWith("//DEPS ") }
        .forEach { implementation(it.removePrefix("//DEPS ").trim()) }

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
        // The JBang header of App.java must keep its `//DEPS` form.
        toggleOffOn()
        googleJavaFormat("2.0.0")
    }
}
