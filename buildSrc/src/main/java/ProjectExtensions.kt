import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.io.FileInputStream
import java.util.Properties

val Project.projectConfig: ProjectConfig
    get() = extensions.findByType(ProjectConfig::class.java)!!

fun Project.setupRoomSchemas() {
    extensions.configure(com.google.devtools.ksp.gradle.KspExtension::class.java) {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

fun LibraryExtension.setupLibraryDefaults(
    projectConfig: ProjectConfig,
) {
    if (projectConfig.compileSdkPreview != null) {
        compileSdkPreview = projectConfig.compileSdkPreview
    } else {
        compileSdk = projectConfig.compileSdk
    }

    defaultConfig {
        minSdk = projectConfig.minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

fun LibraryExtension.setupModuleBuildTypes() {
    buildTypes {
        debug {
            consumerProguardFiles("consumer-rules.pro")
        }
        create("beta") {
            consumerProguardFiles("consumer-rules.pro")
        }
        release {
            consumerProguardFiles("consumer-rules.pro")
        }
    }
}

fun Project.setupKotlinOptions() {
    tasks.withType(KotlinCompile::class.java) {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlin.ExperimentalStdlibApi",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
                "-opt-in=kotlin.time.ExperimentalTime",
                "-jvm-default=no-compatibility",
                "-opt-in=kotlin.uuid.ExperimentalUuidApi",
            )
        }
    }
}

fun CommonExtension.setupCompileOptions() {
    compileOptions.isCoreLibraryDesugaringEnabled = true
    compileOptions.sourceCompatibility = JavaVersion.VERSION_17
    compileOptions.targetCompatibility = JavaVersion.VERSION_17
}

fun com.android.build.api.dsl.SigningConfig.setupCredentials(
    signingPropsPath: File? = null
) {

    val keyStoreFromEnv = System.getenv("STORE_PATH")?.let { File(it) }

    if (keyStoreFromEnv?.exists() == true) {
        println("Using signing data from environment variables.")
        storeFile = keyStoreFromEnv
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEY_PASSWORD")
    } else {
        println("Using signing data from properties file.")
        val props = Properties().apply {
            signingPropsPath?.takeIf { it.canRead() }?.let { load(FileInputStream(it)) }
        }

        val keyStorePath = props.getProperty("release.storePath")?.let { File(it) }

        if (keyStorePath?.exists() == true) {
            storeFile = keyStorePath
            storePassword = props.getProperty("release.storePassword")
            keyAlias = props.getProperty("release.keyAlias")
            keyPassword = props.getProperty("release.keyPassword")
        }
    }
}

fun Test.setupTestJvm() {
    maxHeapSize = "1g"
    maxParallelForks = 1

    val crashDir = File(project.layout.buildDirectory.get().asFile, "test-jvm-crash/$name")
    doFirst { crashDir.mkdirs() }

    jvmArgs(
        "-XX:+HeapDumpOnOutOfMemoryError",
        "-XX:HeapDumpPath=${crashDir.absolutePath}",
        "-XX:ErrorFile=${crashDir.absolutePath}/hs_err_pid%p.log",
    )
}

fun Test.setupTestLogging() {
    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
        )
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true

        addTestListener(object : TestListener {
            override fun beforeSuite(suite: TestDescriptor) {}
            override fun beforeTest(testDescriptor: TestDescriptor) {}
            override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}
            override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                val label = if (suite.parent == null) "TASK RESULT" else "SUITE RESULT"
                val messages = ""${'"'}${'"'}
                    ------------------------------------------------------------------------------------------------
                    | $label: ${'$'}{result.resultType} ${'$'}{result.testCount} tests: ${'$'}{result.successfulTestCount} passed, ${'$'}{result.failedTestCount} failed, ${'$'}{result.skippedTestCount} skipped)
                    ------------------------------------------------------------------------------------------------

                ""${'"'}${'"'}.trimIndent()
                println(messages)

                if (suite.parent == null && result.resultType == TestResult.ResultType.FAILURE && result.failedTestCount == 0L) {
                    println(
                        ""${'"'}${'"'}
                        ################################################################################################
                        # TEST JVM WORKER DEATH SUSPECTED
                        # The test task failed but zero test cases reported a failure (${'$'}{result.skippedTestCount} skipped).
                        # That combination means the worker JVM died instead of the tests failing.
                        # Check the raw Gradle worker output above and build/test-jvm-crash/ for hs_err/heap dumps.
                        ################################################################################################

                        ""${'"'}${'"'}.trimIndent()
                    )
                }
            }
        })
    }
}
