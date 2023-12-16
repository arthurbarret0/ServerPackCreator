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

import de.griefed.serverpackcreator.api.ApiWrapper
import org.apache.logging.log4j.kotlin.cachedLoggerOf
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class WebService(private val api: ApiWrapper) {
    private val log = cachedLoggerOf(this.javaClass)
    private var springBootApplicationContext: ConfigurableApplicationContext? = null

    fun start(args: Array<String>): ConfigurableApplicationContext {
        val lastIndex = "--spring.config.location=classpath:application.properties," +
                "classpath:serverpackcreator.properties," +
                "file:${api.apiProperties.serverPackCreatorPropertiesFile.absolutePath}," +
                "optional:file:./serverpackcreator.properties"
        val springArgs = if (args.isEmpty()) {
            arrayOf(lastIndex)
        } else {
            val temp = args.toList().toTypedArray()
            temp[temp.lastIndex] = lastIndex
            temp.toList().toTypedArray()
        }
        log.debug("Running webservice with args:${springArgs.contentToString()}")
        log.debug("Application name: ${getSpringBootApplicationContext(springArgs).applicationName}")
        log.debug("Property sources:")
        for (property in springBootApplicationContext!!.environment.propertySources) {
            log.debug("    ${property.name}: ${property.source}")
        }
        log.debug("System properties:")
        for ((key, value) in springBootApplicationContext?.environment!!.systemProperties) {
            log.debug("    Key: $key - Value: $value")
        }
        log.debug("System environment:")
        for ((key, value) in springBootApplicationContext!!.environment.systemEnvironment) {
            log.debug("    Key: $key - Value: $value")
        }
        return springBootApplicationContext!!
    }

    /**
     * This instances application context when running as a webservice. When no instance of the Spring
     * Boot application context is available yet, it will be created and the Spring Boot application
     * will be started with the given arguments.
     *
     * @param args CLI arguments to pass to Spring Boot when it has not yet been started.
     * @return Application context of Spring Boot.
     * @author Griefed
     */
    @Synchronized
    fun getSpringBootApplicationContext(args: Array<String>): ConfigurableApplicationContext {
        if (springBootApplicationContext == null) {
            springBootApplicationContext = runApplication<WebService>(*args)
        }
        return springBootApplicationContext!!
    }
}

fun main(args: Array<String>) {
    WebService(ApiWrapper.api()).start(args)
}