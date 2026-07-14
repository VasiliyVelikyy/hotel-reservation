plugins {
	java
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
    id ("jacoco")
}

group = "ru.moskalev"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

ext {
    set("mapstructVersion", "1.6.3")
    set("springdocVersion", "2.8.5")
    set("bouncyCastleVersion", "1.78.1")
    set("opencsvVersion", "5.7.1")
    set("jspecifyVersion", "1.0.0")
    set("testcontainersVersion", "1.20.4")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    implementation("org.mapstruct:mapstruct:${property("mapstructVersion")}")
    implementation("org.postgresql:postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${property("springdocVersion")}")
    implementation("org.bouncycastle:bcprov-jdk18on:${property("bouncyCastleVersion")}")
    implementation("com.opencsv:opencsv:${property("opencsvVersion")}")
    implementation("org.jspecify:jspecify:${property("jspecifyVersion")}")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:${property("mapstructVersion")}")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:junit-jupiter:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:postgresql:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:kafka:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:mongodb:${property("testcontainersVersion")}")
    testImplementation("org.springframework.security:spring-security-test")

    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }


    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/*HotelReservationApplication.class",
                    "**/config/**",
                    "**/dto/**",
                    "**/exception/**",
                    "**/enumeration/**",
                    "**/domain/**",
                    "**/integration/api/**",
                    "**/repo/**",
                    "**/specification/**",
                    "**/interceptor/**",

                )
            }
        })
    )
    doLast {
        val reportFile = reports.html.outputLocation.get().file("index.html").asFile

        if (reportFile.exists()) {
            val reportUrl = "file:///" + reportFile.absolutePath.replace("\\", "/")
            println("  Откройте отчет в браузере: $reportUrl")
        }
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80.toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}


tasks.withType<Test> {
	useJUnitPlatform()
}