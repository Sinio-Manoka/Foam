plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
}

val os = System.getProperty("os.name").lowercase()
val arch = System.getProperty("os.arch").lowercase()

val kdtNativeArtifact = when {
    os.contains("windows") && arch.contains("aarch64") ->
        "kotlin-desktop-toolkit-windows-arm64"

    os.contains("windows") ->
        "kotlin-desktop-toolkit-windows-x64"

    os.contains("mac") && arch.contains("aarch64") ->
        "kotlin-desktop-toolkit-macos-arm64"

    os.contains("mac") ->
        "kotlin-desktop-toolkit-macos-x64"

    os.contains("linux") && arch.contains("aarch64") ->
        "kotlin-desktop-toolkit-linux-arm64"

    os.contains("linux") ->
        "kotlin-desktop-toolkit-linux-x64"

    else -> error("Unsupported platform: $os / $arch")
}

val kdtVersion = "0.0.89"

dependencies {
    implementation(project(":utils"))

    implementation(
        "org.jetbrains.kotlin-desktop-toolkit:$kdtNativeArtifact:$kdtVersion"
    )

    implementation(
        "org.jetbrains.kotlin-desktop-toolkit:kotlin-desktop-toolkit-common:$kdtVersion"
    )

    implementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:0.150.1")

    implementation("dev.vfyjxf:taffy:1.1.4")

    implementation("io.sf.carte:css4j:6.2")

}

val nativeDependencyScope = configurations.dependencyScope("nativeDependencies")
val nativeFiles = configurations.resolvable("nativeFiles") {
    extendsFrom(nativeDependencyScope.get())
    attributes {
        attribute(
            org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.attribute,
            org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm,
        )
        attribute(
            org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE,
            26,
        )
    }
}

dependencies {
    nativeDependencyScope(
        "org.jetbrains.kotlin-desktop-toolkit:$kdtNativeArtifact:$kdtVersion",
    )
}

val extractKdtNative = tasks.register<Copy>("extractKdtNative") {
    group = "kdt"
    description = "Extracts the native Kotlin Desktop Toolkit libraries to build/kdt-native."

    val nativeFilesProvider = nativeFiles
    inputs.files(
        providers.provider<org.gradle.api.file.FileCollection> {
            nativeFilesProvider.get().incoming.files
        },
    )

    from({
        nativeFilesProvider.get().files.map { jar ->
            zipTree(jar).matching { include("*.dll", "*.so", "*.dylib") }
        }
    })
    into(layout.buildDirectory.dir("kdt-native"))
}

application {
    mainClass = "com.foam.app.AppKt"
    applicationDefaultJvmArgs = listOf(
        "--enable-preview",
        "--enable-native-access=ALL-UNNAMED"
    )
}

tasks.named<JavaExec>("run") {
    dependsOn(extractKdtNative)

    val nativeFolder = layout.buildDirectory.dir("kdt-native").map { it.asFile.absolutePath }
    val nativeLog = layout.buildDirectory.file("kdt-native.log").map { it.asFile.absolutePath }

    jvmArgumentProviders.add(
        CommandLineArgumentProvider {
            listOf(
                "-Dkdt.library.folder.path=${nativeFolder.get()}",
                "-Dkdt.native.log.path=${nativeLog.get()}",
            )
        },
    )
}

tasks.register("printRuntimeClasspath") {
    val cp = configurations.runtimeClasspath.get().asPath
    doLast { println(cp) }
}
