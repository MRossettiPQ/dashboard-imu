val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val queryDslVersion: String by project
val blazePersistenceVersion: String by project
val quarkusHibernateTypes: String by project
val reflectionsVersion: String by project
val kotlinVersion: String by project
val authJavaJwt: String by project
val authJavaRsa: String by project
val jacksonVersion: String by project

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
    // === PLATFORMS ===
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:quarkus-blaze-persistence-bom:${quarkusPlatformVersion}"))

    // === KOTLIN ===
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.quarkus:quarkus-kotlin")

    // === WEB / REST ===
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jaxb")
    implementation("io.quarkus:quarkus-rest-jsonb")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-reactive-routes")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    // === QUARKUS CORE ===
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-cache")
    implementation("io.quarkus:quarkus-mutiny")
    implementation("io.quarkus:quarkus-quartz")
    implementation("io.quarkus:quarkus-scheduler")
    implementation("io.quarkus:quarkus-config-yaml")

    // === DATABASE ===
    implementation("org.reflections:reflections:$reflectionsVersion")
    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkiverse.hibernatetypes:quarkus-hibernate-types:$quarkusHibernateTypes")

    // === FLYWAY ===
    implementation("io.quarkus:quarkus-flyway")

    // === SMALLRYE ===
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-smallrye-fault-tolerance")
    implementation("io.quarkus:quarkus-smallrye-context-propagation")

    // === AUTH / OIDC / JWK ===
    implementation("io.quarkus:quarkus-oidc")
    implementation("com.auth0:java-jwt:$authJavaJwt")
    implementation("com.auth0:jwks-rsa:$authJavaRsa")

    // === WEBSOCKET ===
    implementation("io.quarkus:quarkus-websockets")

    // === TEST ===
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")

    // === QUERYDSL ===
    kapt("com.querydsl:querydsl-apt:${queryDslVersion}:jakarta")
    implementation("com.querydsl:querydsl-core:$queryDslVersion")
    implementation("com.querydsl:querydsl-jpa:$queryDslVersion:jakarta")
    implementation("com.querydsl:querydsl-apt:$queryDslVersion:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:$queryDslVersion:jakarta")

    // === BLAZEPERSISTENCE ===
    implementation("com.blazebit:blaze-persistence-integration-quarkus-3:$blazePersistenceVersion")
    implementation("com.blazebit:blaze-persistence-integration-querydsl-expressions-jakarta:$blazePersistenceVersion")
    runtimeOnly("com.blazebit:blaze-persistence-integration-hibernate-6.2:$blazePersistenceVersion")
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