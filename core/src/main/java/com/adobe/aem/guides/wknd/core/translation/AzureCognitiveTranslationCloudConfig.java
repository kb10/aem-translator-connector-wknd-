package com.adobe.aem.guides.wknd.core.translation;

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

    /**
     * Constructor called by AEM when loading cloud config.
     *
     * @param resource The cloud config resource from /conf/
     * @param previewPath Path for preview (can be null)
     */
    public AzureCognitiveTranslationCloudConfig(Resource resource, String previewPath) {
        this.resource = resource;
        this.previewPath = previewPath;
    }

    @Override
    public Map<String, String> getLanguages() throws TranslationException {
        return getLanguages(null);
    }

    public Map<String, String> getLanguages(ResourceResolver resolver) {
        // Return null to indicate all languages are supported
        // Azure Translator supports 100+ languages
        return null;
    }

    @Override
    public Map<String, String> getCategories() throws TranslationException {
        // Return null to indicate default category
        // Could return custom categories if needed
        return null;
    }

    /**
     * Get the underlying resource.
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Get the preview path.
     */
    public String getPreviewPath() {
        return previewPath;
    }

    /**
     * Get the config resource path.
     */
    public String getConfigResourcePath() {
        return resource != null ? resource.getPath() : null;
    }
}
