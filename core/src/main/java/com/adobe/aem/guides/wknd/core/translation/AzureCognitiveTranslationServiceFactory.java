package com.adobe.aem.guides.wknd.core.translation;

import java.util.Collections;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.translation.api.TranslationConstants.TranslationMethod;
import com.adobe.granite.translation.api.TranslationException;
import com.adobe.granite.translation.api.TranslationService;
import com.adobe.granite.translation.api.TranslationServiceFactory;

/**
 * Lufthansa Azure Cognitive Translation Service Factory.
 *
 * This factory is discovered by AEM's Translation Integration Framework (TIF)
 * and appears in the Cloud Config UI.
 */
@Component(
        service = TranslationServiceFactory.class,
        immediate = true,
        property = {
                TranslationServiceFactory.PROPERTY_TRANSLATION_FACTORY + "=lufthansa-azure",
                "translation.provider.id=lufthansa-azure",
                "translation.provider.name=Lufthansa Azure Machine Translation",
                "translation.provider.type=MT"
        }
)
@Designate(ocd = AzureCognitiveTranslationConfig.class)
public class AzureCognitiveTranslationServiceFactory implements TranslationServiceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AzureCognitiveTranslationServiceFactory.class);

    private volatile AzureCognitiveTranslationConfig config;

    @Activate
    @Modified
    protected void activate(AzureCognitiveTranslationConfig config) {
        this.config = config;
        LOG.info("Lufthansa Azure Cognitive Translation Factory activated. Endpoint: {}",
                maskEndpoint(config.endpoint()));
    }

    public TranslationService createTranslationService(TranslationMethod translationMethod,
                                                       String cloudConfigPath,
                                                       Resource resource) throws TranslationException {
        return createTranslationService(translationMethod, cloudConfigPath);
    }

    @Override
    public TranslationService createTranslationService(TranslationMethod translationMethod,
                                                       String cloudConfigPath) throws TranslationException {
        validateConfiguration();
        if (translationMethod != TranslationMethod.MACHINE_TRANSLATION) {
            throw new TranslationException(
                    "Unsupported translation method: " + translationMethod,
                    TranslationException.ErrorCode.SERVICE_NOT_IMPLEMENTED);
        }
        LOG.info("Creating TranslationService. Method: {}, CloudConfig: {}",
                translationMethod, cloudConfigPath);
        return new AzureCognitiveTranslationService(config);
    }

    @Override
    public List<TranslationMethod> getSupportedTranslationMethods() {
        return Collections.singletonList(TranslationMethod.MACHINE_TRANSLATION);
    }

    @Override
    public String getServiceFactoryName() {
        return config.providerName();
    }

    @Override
    public Class<?> getServiceCloudConfigClass() {
        return AzureCognitiveTranslationCloudConfig.class;
    }

    private String maskEndpoint(String endpoint) {
        if (endpoint == null || endpoint.length() < 20) {
            return "***";
        }
        return endpoint.substring(0, 20) + "...";
    }

    private void validateConfiguration() throws TranslationException {
        if (config == null) {
            throw new TranslationException(
                    "Azure translation factory is not configured",
                    TranslationException.ErrorCode.MISSING_CREDENTIALS);
        }
        if (isBlank(config.endpoint()) || isBlank(config.subscriptionKey())) {
            throw new TranslationException(
                    "Azure endpoint and subscription key must be configured",
                    TranslationException.ErrorCode.MISSING_CREDENTIALS);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
