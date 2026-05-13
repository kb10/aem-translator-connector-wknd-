package com.adobe.aem.guides.wknd.core.translation;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * OSGi configuration for Custom Azure Cognitive Translation Connector.
 */
@ObjectClassDefinition(
        name = "Custom Azure Cognitive Translation Connector",
        description = "Azure Cognitive Translator connector for AEM 6.5"
)
public @interface AzureCognitiveTranslationConfig {

    @AttributeDefinition(
            name = "Azure Endpoint",
            description = "Full URL to Azure Translator API (e.g., https://example.cognitiveservices.azure.com/translator/text/v3.0/translate)"
    )
    String endpoint() default "https://example.cognitiveservices.azure.com/translator/text/v3.0/translate";

    @AttributeDefinition(
            name = "Subscription Key",
            type = AttributeType.PASSWORD,
            description = "Azure Cognitive Services subscription key (Ocp-Apim-Subscription-Key)"
    )
    String subscriptionKey() default "";

    @AttributeDefinition(
            name = "Region (optional)",
            description = "Azure region for Ocp-Apim-Subscription-Region header (leave empty if not required)"
    )
    String region() default "";

    @AttributeDefinition(
            name = "Text Type",
            description = "Content type: html or plain"
    )
    String textType() default "html";

    @AttributeDefinition(
            name = "Connection Timeout (ms)"
    )
    int connectTimeoutMs() default 5000;

    @AttributeDefinition(
            name = "Socket Timeout (ms)"
    )
    int socketTimeoutMs() default 30000;

    @AttributeDefinition(
            name = "Max Items Per Batch",
            description = "Azure limit: 100"
    )
    int maxItemsPerBatch() default 100;

    @AttributeDefinition(
            name = "Max Characters Per Batch",
            description = "Azure limit: 50000"
    )
    int maxCharsPerBatch() default 50000;

    @AttributeDefinition(
            name = "Provider Name",
            description = "Display name in AEM Translation UI"
    )
    String providerName() default "My Custom Translator";
}
