plugins {
    id("java")
}

group = "net.cosyfluf"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.jbox2d:jbox2d-library:2.2.1.1")
}

tasks.test {
    useJUnitPlatform()
}