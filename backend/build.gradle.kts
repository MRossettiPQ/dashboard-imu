val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val queryDslVersion: String by project
val blazePersistenceVersion: String by project
val quarkusHibernateTypes: String by project
val reflectionsVersion: String by project
val socketIoVersion: String by project
val moquetteVersion: String by project
val jacksonVersion: String by project
val jmdnsVersion: String by project
val mdnsVersion: String by project
val sqliteVersion: String by project
val jSerialCommVersion: String by project
val oshiVersion: String by project
val jnaVersion: String by project

apply(from = "pem.gradle.kts")
apply(from = "vue.gradle.kts")

plugins {
    val kotlinVersion = "2.0.21"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    id("io.quarkus")
    id("idea")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // === QUARKUS PLATFORM (BoM) ===
    implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))
    implementation(enforcedPlatform("$quarkusPlatformGroupId:quarkus-blaze-persistence-bom:$quarkusPlatformVersion"))

    // === KOTLIN ===
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.quarkus:quarkus-kotlin")

    // === QUARKUS CORE ===
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-cache")
    implementation("io.quarkus:quarkus-mutiny")
    implementation("io.quarkus:quarkus-quartz")
    implementation("io.quarkus:quarkus-scheduler")
    implementation("io.quarkus:quarkus-config-yaml")

    // === REST / JSON / WEBSERVICES ===
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jaxb")
    implementation("io.quarkus:quarkus-rest-jsonb")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    // === FRONTEND / SPA / HTTP ===
    implementation("io.quarkus:quarkus-vertx-http")
    implementation("io.quarkus:quarkus-reactive-routes")

    // === DATABASE / ORM ===
    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-flyway")
    implementation("org.reflections:reflections:$reflectionsVersion")
    implementation("io.quarkiverse.hibernatetypes:quarkus-hibernate-types:$quarkusHibernateTypes")
    implementation("io.quarkiverse.jdbc:quarkus-jdbc-sqlite:$sqliteVersion")

    // === QUERYDSL ===
    implementation("com.querydsl:querydsl-core:$queryDslVersion")
    implementation("com.querydsl:querydsl-jpa:$queryDslVersion:jakarta")
    implementation("com.querydsl:querydsl-apt:$queryDslVersion:jakarta")
    kapt("com.querydsl:querydsl-apt:$queryDslVersion:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:$queryDslVersion:jakarta")

    // === BLAZE PERSISTENCE ===
    implementation("com.blazebit:blaze-persistence-integration-quarkus-3:$blazePersistenceVersion")
    implementation("com.blazebit:blaze-persistence-integration-querydsl-expressions-jakarta:$blazePersistenceVersion")
    runtimeOnly("com.blazebit:blaze-persistence-integration-hibernate-6.2:$blazePersistenceVersion")

    // === SMALLRYE ===
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("io.quarkus:quarkus-smallrye-jwt-build")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-smallrye-fault-tolerance")
    implementation("io.quarkus:quarkus-smallrye-context-propagation")

    // === WEBSOCKET / SOCKET.IO ===
    implementation("io.quarkus:quarkus-websockets")
    implementation("com.corundumstudio.socketio:netty-socketio:$socketIoVersion")

    // === MQTT ===
    implementation("io.moquette:moquette-broker:$moquetteVersion")

    // === MDNS ===
    implementation("org.jmdns:jmdns:$jmdnsVersion")
    implementation("io.quarkiverse.mdns:quarkus-mdns:$mdnsVersion")

    // === TESTING ===
    testImplementation("io.quarkus:quarkus-jacoco")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation(kotlin("test"))
}

group = "com.rot"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

java.sourceSets["main"].java {
    srcDir("src/main")
}

tasks.withType<JavaCompile> {
    options.annotationProcessorPath = configurations["kapt"]
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        javaParameters = true
    }
}

System.setProperty("quarkus.config.locations", "application.yml,application.base.yml")
System.setProperty("quarkus.analytics.disabled", "true")