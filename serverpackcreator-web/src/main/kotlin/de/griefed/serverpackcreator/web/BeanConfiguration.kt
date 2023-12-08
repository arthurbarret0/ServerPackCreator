/* Copyright (C) 2023  Griefed
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 *
 * The full license can be found at https:github.com/Griefed/ServerPackCreator/blob/main/LICENSE
 */
package de.griefed.serverpackcreator.web

import com.electronwill.nightconfig.toml.TomlParser
import com.fasterxml.jackson.databind.ObjectMapper
import de.griefed.serverpackcreator.api.*
import de.griefed.serverpackcreator.api.modscanning.*
import de.griefed.serverpackcreator.api.utilities.common.*
import de.griefed.serverpackcreator.api.versionmeta.VersionMeta
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import java.io.File
import java.util.*
import javax.sql.DataSource
import javax.xml.parsers.DocumentBuilderFactory


/**
 * Bean configuration for running ServerPackCreator as a webservice. This class provides beans for a
 * couple or properties which can not otherwise be provided.
 *
 * @author Griefed
 */
@Configuration
@Suppress("unused")
class BeanConfiguration @Autowired constructor() {

    @Bean
    fun apiWrapper(): ApiWrapper {
        return ApiWrapper.api()
    }

    @Bean
    fun applicationProperties(): ApiProperties {
        return apiWrapper().apiProperties
    }

    @Bean
    fun utilities(): Utilities {
        return apiWrapper().utilities
    }

    @Bean
    fun annotationScanner(): AnnotationScanner {
        return apiWrapper().annotationScanner
    }

    @Bean
    fun applicationPlugins(): ApiPlugins {
        return apiWrapper().apiPlugins
    }

    @Bean
    fun booleanUtilities(): BooleanUtilities {
        return apiWrapper().booleanUtilities
    }

    @Bean
    fun configurationHandler(): ConfigurationHandler {
        return apiWrapper().configurationHandler
    }

    @Bean
    fun fabricScanner(): FabricScanner {
        return apiWrapper().fabricScanner
    }

    @Bean
    fun fileUtilities(): FileUtilities {
        return apiWrapper().fileUtilities
    }

    @Bean
    fun jarUtilities(): JarUtilities {
        return apiWrapper().jarUtilities
    }

    @Bean
    fun jsonUtilities(): JsonUtilities {
        return apiWrapper().jsonUtilities
    }

    @Bean
    fun listUtilities(): ListUtilities {
        return apiWrapper().listUtilities
    }

    @Bean
    fun modScanner(): ModScanner {
        return apiWrapper().modScanner
    }

    @Bean
    fun quiltScanner(): QuiltScanner {
        return apiWrapper().quiltScanner
    }

    @Bean
    fun serverPackHandler(): ServerPackHandler {
        return apiWrapper().serverPackHandler
    }

    @Bean
    fun stringUtilities(): StringUtilities {
        return apiWrapper().stringUtilities
    }

    @Bean
    fun systemUtilities(): SystemUtilities {
        return apiWrapper().systemUtilities
    }

    @Bean
    fun tomlScanner(): TomlScanner {
        return apiWrapper().tomlScanner
    }

    @Bean
    fun versionMeta(): VersionMeta {
        return apiWrapper().versionMeta
    }

    @Bean
    fun webUtilities(): WebUtilities {
        return apiWrapper().webUtilities
    }

    @Bean
    fun xmlUtilities(): XmlUtilities {
        return apiWrapper().xmlUtilities
    }

    @Bean
    fun minecraftManifest(): File {
        return apiWrapper().apiProperties.minecraftVersionManifest
    }

    @Bean
    fun forgeManifest(): File {
        return apiWrapper().apiProperties.forgeVersionManifest
    }

    @Bean
    fun neoForgeManifest(): File {
        return apiWrapper().apiProperties.neoForgeVersionManifest
    }

    @Bean
    fun fabricManifest(): File {
        return apiWrapper().apiProperties.fabricVersionManifest
    }

    @Bean
    fun fabricIntermediariesManifest(): File {
        return apiWrapper().apiProperties.fabricIntermediariesManifest
    }

    @Bean
    fun fabricInstallerManifest(): File {
        return apiWrapper().apiProperties.fabricInstallerManifest
    }

    @Bean
    fun quiltManifest(): File {
        return apiWrapper().apiProperties.quiltVersionManifest
    }

    @Bean
    fun quiltInstallerManifest(): File {
        return apiWrapper().apiProperties.quiltInstallerManifest
    }

    @Bean
    fun args(): Array<String?> {
        return arrayOfNulls(0)
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        return apiWrapper().objectMapper
    }

    @Bean
    fun tomlParser(): TomlParser {
        return apiWrapper().tomlParser
    }

    @Bean
    fun legacyFabricGameManifest(): File {
        return apiWrapper().apiProperties.legacyFabricGameManifest
    }

    @Bean
    fun legacyFabricLoaderManifest(): File {
        return apiWrapper().apiProperties.legacyFabricLoaderManifest
    }

    @Bean
    fun legacyFabricInstallerManifest(): File {
        return apiWrapper().apiProperties.legacyFabricInstallerManifest
    }

    @Bean
    fun documentBuilder(): DocumentBuilderFactory {
        return apiWrapper().documentBuilderFactory
    }

    @Autowired
    var env: Environment? = null

    /*@Bean
    fun dataSource(): DataSource {
        val dataSource = DriverManagerDataSource()
        env!!.getProperty("spring.datasource.driver-class-name")?.let { dataSource.setDriverClassName(it) }
        dataSource.url = env!!.getProperty("spring.datasource.url")
        return dataSource
    }

    @Bean
    fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val em = LocalContainerEntityManagerFactoryBean()
        em.setDataSource(dataSource())
        em.setPackagesToScan(*arrayOf("de.griefed.serverpackcreator.web"))
        em.jpaVendorAdapter = HibernateJpaVendorAdapter()
        em.setJpaProperties(additionalProperties())
        return em
    }

    fun additionalProperties(): Properties {
        val hibernateProperties = Properties()
        if (env!!.getProperty("spring.jpa.hibernate.ddl-auto") != null) {
            hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env!!.getProperty("spring.jpa.hibernate.ddl-auto"))
        }
        if (env!!.getProperty("spring.jpa.database-platform") != null) {
            hibernateProperties.setProperty("hibernate.dialect", env!!.getProperty("spring.jpa.database-platform"))
        }
        if (env!!.getProperty("hibernate.show_sql") != null) {
            hibernateProperties.setProperty("hibernate.show_sql", env!!.getProperty("hibernate.show_sql"))
        }
        return hibernateProperties
    }*/
}