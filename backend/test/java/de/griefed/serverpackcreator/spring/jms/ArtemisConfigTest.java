package de.griefed.serverpackcreator.spring.jms;

import de.griefed.serverpackcreator.ApplicationProperties;
import de.griefed.serverpackcreator.DefaultFiles;
import de.griefed.serverpackcreator.i18n.LocalizationManager;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.jms.support.destination.JmsDestinationAccessor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ArtemisConfigTest {

    private static final Logger LOG = LogManager.getLogger(ArtemisConfigTest.class);

    private final JmsTemplate jmsTemplate;

    private final String QUEUE_UNIQUE_ID = "unique_id";
    private final String QUEUE_TASKS = "tasks.background";
    private final DefaultFiles DEFAULTFILES;
    private final LocalizationManager LOCALIZATIONMANAGER;
    private final ApplicationProperties APPLICATIONPROPERTIES;

    @Autowired
    ArtemisConfigTest(JmsTemplate injectedJmsTemplate) {
        try {
            FileUtils.copyFile(new File("backend/main/resources/serverpackcreator.properties"),new File("serverpackcreator.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.APPLICATIONPROPERTIES = new ApplicationProperties();

        this.jmsTemplate = injectedJmsTemplate;

        this.jmsTemplate.setReceiveTimeout(JmsDestinationAccessor.RECEIVE_TIMEOUT_NO_WAIT);

        LOCALIZATIONMANAGER = new LocalizationManager(APPLICATIONPROPERTIES);
        LOCALIZATIONMANAGER.initialize();
        DEFAULTFILES = new DefaultFiles(LOCALIZATIONMANAGER, APPLICATIONPROPERTIES);
        DEFAULTFILES.filesSetup();
        DEFAULTFILES.checkDatabase();
    }

    @AfterEach
    void emptyQueue() {
        while(jmsTemplate.receive(QUEUE_TASKS) != null) {
            LOG.info("Emptying queue");
        }
    }

    @Test
    void noUniqueID() {
        for (int i = 1; i < 6; i++) {
            LOG.info("Sending message " + i);
            jmsTemplate.convertAndSend(QUEUE_TASKS, "message " + i);
        }

        int size = jmsTemplate.browse(QUEUE_TASKS, (session, browser) -> Collections.list(browser.getEnumeration()).size());

        String message = Objects.requireNonNull(jmsTemplate.receiveAndConvert(QUEUE_TASKS)).toString();

        Assertions.assertEquals(5, size);
        Assertions.assertEquals("message 1", message);
    }

    @Test
    void sameID() {
        for (int i = 1; i < 6; i++) {
            LOG.info("Sending message " + i);

            jmsTemplate.convertAndSend(QUEUE_TASKS, "message " + i, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws JMSException {
                    message.setStringProperty(QUEUE_UNIQUE_ID, "1");
                    return message;
                }
            });
        }

        int size = jmsTemplate.browse(QUEUE_TASKS, (session, browser) -> Collections.list(browser.getEnumeration()).size());

        String message = Objects.requireNonNull(jmsTemplate.receiveAndConvert(QUEUE_TASKS)).toString();

        Assertions.assertEquals(1, size);
        Assertions.assertEquals("message 5", message);
    }

}