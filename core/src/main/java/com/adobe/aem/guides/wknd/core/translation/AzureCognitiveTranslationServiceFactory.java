package com.adobe.aem.guides.wknd.core.translation;

import java.util.ArrayList;
import java.util.List;

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
 * Azure Cognitive Translation Service Factory for WKND demo.
 */
@Component(
        service = TranslationServiceFactory.class,
        immediate = true,
        property = {
                TranslationServiceFactory.PROPERTY_TRANSLATION_FACTORY + "=" + AzureCognitiveTranslationServiceFactory.PROVIDER_ID,
                "translation.provider.id=" + AzureCognitiveTranslationServiceFactory.PROVIDER_ID,
                "translation.provider.name=" + AzureCognitiveTranslationServiceFactory.PROVIDER_LABEL,
                "translation.provider.type=MT"
        }
)
@Designate(ocd = AzureCognitiveTranslationConfig.class)
public class AzureCognitiveTranslationServiceFactory implements TranslationServiceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AzureCognitiveTranslationServiceFactory.class);

    /** Stable internal identifier used by AEM/framework matching. */
    public static final String PROVIDER_ID = "wknd-azure";

    /** Human-readable label used in the AEM UI. */
    public static final String PROVIDER_LABEL = "WKND Azure Machine Translation";

    private volatile AzureCognitiveTranslationConfig config;
    private volatile boolean configValid;

    @Activate
    @Modified
    protected void activate(AzureCognitiveTranslationConfig config) {
        this.config = config;
        this.configValid = validateConfig(config);

        LOG.info("========================================");
        LOG.info("WKND AZURE TRANSLATION FACTORY ACTIVATING");
        LOG.info("========================================");
        LOG.info("Provider ID   : {}", PROVIDER_ID);
        LOG.info("Provider Label: {}", PROVIDER_LABEL);

        if (configValid) {
            LOG.info("Factory activated successfully. Endpoint: {}", maskEndpoint(config.endpoint()));
        } else {
            LOG.error("Factory activated with INVALID configuration.");
        }
    }

    private boolean validateConfig(AzureCognitiveTranslationConfig cfg) {
        if (cfg == null) {
            LOG.error("Azure translation configuration is null");
            return false;
        }
        if (isBlank(cfg.endpoint())) {
            LOG.error("Azure endpoint is missing");
            return false;
        }
        if (isBlank(cfg.subscriptionKey())) {
            LOG.error("Azure subscription key is missing");
            return false;
        }
        return true;
    }

    @Override
    public TranslationService createTranslationService(TranslationMethod translationMethod,
                                                       String cloudConfigPath) throws TranslationException {
        LOG.info("Creating TranslationService. method={}, cloudConfigPath={}, providerId={}",
                translationMethod, cloudConfigPath, PROVIDER_ID);

        if (!configValid) {
            throw new TranslationException(
                    "WKND Azure Translation connector is not properly configured. Check OSGi endpoint/subscription key.",
                    TranslationException.ErrorCode.GENERAL_EXCEPTION);
        }

        if (translationMethod != TranslationMethod.MACHINE_TRANSLATION) {
            LOG.warn("Requested translationMethod={} but only MACHINE_TRANSLATION is supported. Returning MT service.", translationMethod);
        }

        String effectiveLabel = PROVIDER_LABEL;
        if (config != null && !isBlank(config.providerName())) {
            effectiveLabel = config.providerName().trim();
        }

        return new AzureCognitiveTranslationService(config, PROVIDER_ID, effectiveLabel);
    }

    @Override
    public List<TranslationMethod> getSupportedTranslationMethods() {
        List<TranslationMethod> methods = new ArrayList<>();
        methods.add(TranslationMethod.MACHINE_TRANSLATION);
        return methods;
    }

    @Override
    public String getServiceFactoryName() {
        return PROVIDER_ID;
    }

    @Override
    public Class<?> getServiceCloudConfigClass() {
        return AzureCognitiveTranslationCloudConfig.class;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String maskEndpoint(String endpoint) {
        if (endpoint == null || endpoint.length() < 24) {
            return "***";
        }
        return endpoint.substring(0, 24) + "...";
    }
}
