package com.adobe.aem.guides.wknd.core.translation;

import java.util.Collections;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.adobe.granite.translation.api.TranslationConfig;
import com.adobe.granite.translation.api.TranslationException;

/**
 * Cloud Configuration class for Azure Cognitive Translation.
 *
 * This class is required by AEM's Translation Integration Framework to
 * read configuration from Cloud Config nodes in /conf/.
 */
public class AzureCognitiveTranslationCloudConfig implements TranslationConfig {

    private final Resource resource;
    private final String previewPath;

    public AzureCognitiveTranslationCloudConfig(Resource resource, String previewPath) {
        this.resource = resource;
        this.previewPath = previewPath;
    }

    @Override
    public Map<String, String> getLanguages() throws TranslationException {
        return Collections.emptyMap();
    }

    /**
     * Cloud Service SDK adds this method to TranslationConfig.
     * On AEM 6.5 it does not exist in the interface, so no @Override.
     * Keeping it here for forward compatibility.
     */
    public Map<String, String> getLanguages(ResourceResolver resourceResolver) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getCategories() throws TranslationException {
        return Collections.emptyMap();
    }

    public Resource getResource() {
        return resource;
    }

    public String getPreviewPath() {
        return previewPath != null ? previewPath : "";
    }

    public String getConfigResourcePath() {
        return resource != null ? resource.getPath() : "";
    }
}
