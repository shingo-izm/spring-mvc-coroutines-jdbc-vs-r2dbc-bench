plugins {
    // Kotlin 2.2 系の最新安定版
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"

    // Spring Boot 最新安定版
    id("org.springframework.boot") version "3.5.5"

    // 依存管理（BOM）。最新版に更新
    id("io.spring.dependency-management") version "1.1.7"
}

group = "dev.sizumikawa"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories { mavenCentral() }

dependencies {
    // MVC
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Coroutines（Boot は管理しないため、明示的に最新版を指定）
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")

    // JDBC（バージョンは Boot BOM に委ねる）
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")

    // R2DBC（バージョンは Boot BOM に委ねる）
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    runtimeOnly("org.postgresql:r2dbc-postgresql")

    // Flyway（スキーマ管理。バージョンは Boot BOM に委ねる）
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Kotlinデータクラスのシリアライズ向け
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}