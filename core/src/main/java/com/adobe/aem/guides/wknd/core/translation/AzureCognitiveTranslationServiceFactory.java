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
 * Custom Azure Cognitive Translation Service Factory.
 */
@Component(
        service = TranslationServiceFactory.class,
        immediate = true,
        property = {
                "translationFactory=my-custom-translator",
                "translation.provider.id=my-custom-translator",
                "translation.provider.name=my-custom-translator",
                "translation.provider.type=MT"
        }
)
@Designate(ocd = AzureCognitiveTranslationConfig.class)
public class AzureCognitiveTranslationServiceFactory implements TranslationServiceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(AzureCognitiveTranslationServiceFactory.class);

    public static final String PROVIDER_ID = "my-custom-translator";
    public static final String DEFAULT_PROVIDER_LABEL = "my-custom-translator";
    public static final String CLOUD_CONFIG_ROOT_PATH = "/conf/global/settings/cloudconfigs/translation/my-custom-translator";
    public static final String DEFAULT_CLOUD_CONFIG_PATH = CLOUD_CONFIG_ROOT_PATH + "/default_config";

    private volatile AzureCognitiveTranslationConfig config;
    private volatile boolean configValid = false;

    @Activate
    @Modified
    protected void activate(AzureCognitiveTranslationConfig config) {

        LOG.info("========================================");
        LOG.info("my-custom-translator FACTORY ACTIVATING NOW!");
        LOG.info("========================================");

        this.config = config;
        this.configValid = validateConfig(config);

        if (configValid) {
            LOG.info("my-custom-translator Factory activated. Provider ID: {}, Endpoint: {}",
                    PROVIDER_ID, maskEndpoint(config.endpoint()));
        } else {
            LOG.error("my-custom-translator Factory activated with INVALID config.");
        }
    }

    private boolean validateConfig(AzureCognitiveTranslationConfig config) {
        if (config == null) {
            LOG.error("Configuration is null");
            return false;
        }
        if (config.endpoint() == null || config.endpoint().trim().isEmpty()) {
            LOG.error("Azure endpoint is missing or empty");
            return false;
        }
        if (config.subscriptionKey() == null || config.subscriptionKey().trim().isEmpty()) {
            LOG.error("Azure subscription key is missing or empty");
            return false;
        }
        return true;
    }

    @Override
    public TranslationService createTranslationService(TranslationMethod translationMethod,
                                                       String cloudConfigPath) throws TranslationException {
        LOG.info("Creating TranslationService. Method: {}, CloudConfig: {}, Provider: {}",
                translationMethod, cloudConfigPath, PROVIDER_ID);

        if (!configValid) {
            throw new TranslationException(
                    "my-custom-translator connector is not properly configured.",
                    TranslationException.ErrorCode.GENERAL_EXCEPTION);
        }

        return new AzureCognitiveTranslationService(config, PROVIDER_ID, getProviderLabel());
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

    private String getProviderLabel() {
        if (config == null || config.providerName() == null || config.providerName().trim().isEmpty()) {
            return DEFAULT_PROVIDER_LABEL;
        }
        return config.providerName().trim();
    }

    private String maskEndpoint(String endpoint) {
        if (endpoint == null || endpoint.length() < 20) {
            return "***";
        }
        return endpoint.substring(0, 20) + "...";
    }
}
