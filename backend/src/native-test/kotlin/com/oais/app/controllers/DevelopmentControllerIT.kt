package com.oais.app.controllers

import com.rot.core.config.ApplicationConfig
import io.quarkus.test.junit.QuarkusIntegrationTest

@QuarkusIntegrationTest
class DevelopmentControllerIT(applicationConfig: ApplicationConfig) : DevelopmentControllerTest(applicationConfig)
