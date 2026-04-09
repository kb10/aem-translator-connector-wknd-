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

    @Override
    public TranslationService createTranslationService(TranslationMethod translationMethod,
                                                       String cloudConfigPath) throws TranslationException {
        LOG.info("Creating TranslationService. Method: {}, CloudConfig: {}",
                translationMethod, cloudConfigPath);
        return new AzureCognitiveTranslationService(config);
    }

    @Override
    public List<TranslationMethod> getSupportedTranslationMethods() {
        List<TranslationMethod> methods = new ArrayList<>();
        methods.add(TranslationMethod.MACHINE_TRANSLATION);
        return methods;
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
}
