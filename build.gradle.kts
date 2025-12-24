fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)
fun projectProperties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.10.5"
    id("org.jetbrains.changelog") version "2.5.0"
}

val pluginId = projectProperties("pluginId")
val pluginName = projectProperties("pluginName")
val pluginDescription = projectProperties("pluginDescription")
val pluginGroup = projectProperties("pluginGroup")
val pluginVersion = projectProperties("pluginVersion")
val pluginSinceBuild = projectProperties("pluginSinceBuild")
val pluginUntilBuild = projectProperties("pluginUntilBuild")
val vendorName = projectProperties("vendorName")
val vendorEmail = projectProperties("vendorEmail")
val pluginUrl = projectProperties("pluginUrl")
val platformVersion = projectProperties("platformVersion")
val platformPlugins = properties("platformPlugins").map { it.split(',') }
group = "org.mustabelmo"
version = "1.0.0"
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        intellijDependencies()
    }
}
intellijPlatform {
    pluginConfiguration {
        id = pluginId
        name = pluginName
        version = pluginVersion
        changeNotes = "TODO"
        description = pluginDescription

        ideaVersion {
            sinceBuild.set(pluginSinceBuild)
        }
        vendor {
            name.set(vendorName)
            email.set(vendorEmail)
            url.set(pluginUrl)
        }
    }
    publishing {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
    signing {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }
}

dependencies {
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.0")

    intellijPlatform {
        intellijIdea("2024.3")
        bundledPlugin("com.intellij.java")
        pluginVerifier()
    }
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
changelog {
    version = "1.0.0"
    group = "record"
}
tasks {
    test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
    }
    patchPluginXml {
        version = "0.0.0"
        changeNotes = ""
        println("HERE")
    }
}
