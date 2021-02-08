val vs = versions()

buildscript {
    repositories {
        // Add here whatever repositories you're already using
        mavenCentral()
    }

//    dependencies {
//        classpath("ch.epfl.scala:gradle-bloop_2.12:1.4.6") // no support for multi-module
//    }
}

plugins {
//    base
    java
    `java-test-fixtures`

    scala
    kotlin("jvm") version "1.4.30" // TODO: remove?

    idea

    `maven-publish`

    id("com.github.ben-manes.versions" ) version "0.36.0"
}

val rootID = vs.projectRootID

allprojects {

    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "java-test-fixtures")

    apply(plugin = "scala")
    apply(plugin = "kotlin")

    apply(plugin = "idea")

    apply(plugin = "maven-publish")


    group = vs.projectGroup
    version = vs.projectV

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlin-dev")
    }

    // resolving jar hells
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("com.chuusai:shapeless_${vs.scalaBinaryV}")).apply {
                with(module("com.chuusai:shapeless_${vs.scalaBinaryV}:${vs.shapelessV}"))
            }
        }
    }

    dependencies {

        compileOnly("${vs.scalaGroup}:scala-compiler:${vs.scalaV}")

        compileOnly("${vs.scalaGroup}:scala-library:${vs.scalaV}")
        testFixturesCompileOnly("${vs.scalaGroup}:scala-library:${vs.scalaV}")
        testCompileOnly("${vs.scalaGroup}:scala-library:${vs.scalaV}")
        // This is a dirty hack to circumvent https://youtrack.jetbrains.com/issue/SCL-17284

        compileOnly("${vs.scalaGroup}:scala-reflect:${vs.scalaV}")

        //https://github.com/tek/splain
        if (vs.splainV !=null)
            scalaCompilerPlugins("io.tryp:splain_${vs.scalaV}:${vs.splainV}")

//        compileOnly(kotlin("stdlib"))
//        compileOnly(kotlin("stdlib-jdk8"))

        api("eu.timepit:singleton-ops_${vs.scalaBinaryV}:0.5.2") // used by all modules

//        api("eu.timepit:singleton-ops_${vs.scalaBinaryV}:0.5.0+22-59783019+20200731-1305-SNAPSHOT")

        testImplementation("org.scalatest:scalatest_${vs.scalaBinaryV}:${vs.scalatestV}")
        testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")

        // TODO: alpha project, switch to mature solution once https://github.com/scalatest/scalatest/issues/1454 is solved
        testRuntimeOnly("co.helmethair:scalatest-junit-runner:0.1.8")

//        testRuntimeOnly("com.vladsch.flexmark:flexmark-all:0.35.10")

    }

    task("dependencyTree") {

        dependsOn("dependencies")
    }

    tasks {
        val jvmTarget = JavaVersion.VERSION_1_8.toString()

        withType<ScalaCompile> {

            targetCompatibility = jvmTarget

            scalaCompileOptions.apply {

//                    isForce = true

                loggingLevel = "verbose"

                val compilerOptions = mutableListOf(
                    "-encoding", "utf8",
                    "-unchecked",
                    "-deprecation",
                    "-feature",
//                            "-Xfatal-warnings",

                    "-Xlint:poly-implicit-overload",
                    "-Xlint:option-implicit",

                    "-Xlog-implicits",
                    "-Xlog-implicit-conversions",

                    "-Yissue-debug"
//                    ,
//                    "-Ytyper-debug",
//                    "-Vtyper"

                    // the following only works on scala 2.13
//                        ,
//                        "-Xlint:implicit-not-found",
//                        "-Xlint:implicit-recursion"

                )

                if (vs.splainV != null) {
                    compilerOptions.addAll(
                        listOf(
                            //splain
                            "-P:splain:tree",
                            "-P:splain:breakinfix:80",
                            "-P:splain:bounds:true",
                            "-P:splain:boundsimplicits:true",
                            "-P:splain:keepmodules:2"
                        )
                    )
                }

                additionalParameters = compilerOptions

                forkOptions.apply {

                    memoryInitialSize = "1g"
                    memoryMaximumSize = "4g"

                    // this may be over the top but the test code in macro & core frequently run implicit search on church encoded Nat type
                    jvmArgs = listOf(
                        "-Xss256m"
                    )
                }
            }
        }

//        kotlin {}
// TODO: remove, kotlin is not in scope at the moment
//
//        withType<KotlinCompile> {
//
//
//            kotlinOptions.jvmTarget = jvmTarget
////            kotlinOptions.freeCompilerArgs += "-XXLanguage:+NewInference"
//            // TODO: re-enable after kotlin compiler argument being declared safe
//        }

        test {

            minHeapSize = "1024m"
            maxHeapSize = "4096m"

            useJUnitPlatform {
                includeEngines("scalatest")
                testLogging {
                    events("passed", "skipped", "failed")
                }
            }

            testLogging {
//                events = setOf(org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED, org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED, org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED, org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT)
//                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                showExceptions = true
                showCauses = true
                showStackTraces = true

                // stdout is used for occasional manual verification
                showStandardStreams = true
            }
        }
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }
//    scala {
//        this.zincVersion
//    }


    publishing {
        val moduleID = if (project.name.startsWith(rootID)) project.name
        else rootID + "-" + project.name

        publications {
            create<MavenPublication>("maven") {
                groupId = groupId
                artifactId = moduleID
                version = version

                from(components["java"])

                suppressPomMetadataWarningsFor("testFixturesApiElements")
                suppressPomMetadataWarningsFor("testFixturesRuntimeElements")
            }
        }
    }


    idea {

        targetVersion = "2020"

        module {

            excludeDirs = excludeDirs + listOf(
                        file(".gradle"),
                        file(".github"),

                        file ("target"),
//                        file ("out"),

                        file(".idea"),
                        file(".vscode"),
                        file(".bloop"),
                        file(".bsp"),
                        file(".metals"),

                        file("logs"),

                        // apache spark
                        file("warehouse")
                    )

            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }
}