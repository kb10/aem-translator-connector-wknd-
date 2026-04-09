package com.adobe.aem.guides.wknd.core.translation;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * OSGi configuration for the Azure Cognitive Translation connector.
 */
@ObjectClassDefinition(
        name = "WKND Azure Cognitive Translation Connector",
        description = "Configuration for the Azure Cognitive Translator integration"
)
public @interface AzureCognitiveTranslationConfig {

    @AttributeDefinition(
            name = "Azure Endpoint",
            description = "Full Azure Translator translate endpoint URL"
    )
    String endpoint() default "";

    @AttributeDefinition(
            name = "Subscription Key",
            type = AttributeType.PASSWORD,
            description = "Azure Cognitive Services subscription key"
    )
    String subscriptionKey() default "";

    @AttributeDefinition(
            name = "Region",
            description = "Optional Azure region for the subscription"
    )
    String region() default "";

    @AttributeDefinition(
            name = "Text Type",
            description = "Request text type sent to Azure: html or plain"
    )
    String textType() default "html";

    @AttributeDefinition(name = "Connection Timeout (ms)")
    int connectTimeoutMs() default 5000;

    @AttributeDefinition(name = "Socket Timeout (ms)")
    int socketTimeoutMs() default 30000;

    @AttributeDefinition(
            name = "Max Items Per Batch",
            description = "Maximum number of strings per Azure request"
    )
    int maxItemsPerBatch() default 100;

    @AttributeDefinition(
            name = "Max Characters Per Batch",
            description = "Maximum characters per Azure request"
    )
    int maxCharsPerBatch() default 50000;

    @AttributeDefinition(
            name = "Provider Name",
            description = "Display name shown in the AEM translation UI"
    )
    String providerName() default "WKND Azure Machine Translation";
}
