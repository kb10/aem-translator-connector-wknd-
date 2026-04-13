package com.adobe.aem.guides.wknd.core.translation;
import java.util.Collections;
import java.util.Map;

import org.apache.sling.api.resource.Resource;

import com.adobe.granite.translation.api.TranslationConfig;
import com.adobe.granite.translation.api.TranslationException;

/**
 * Cloud Configuration class for Lufthansa Azure Cognitive Translation.
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
        // Return empty map instead of null - AEM 6.5 expects non-null
        // Empty map indicates all languages are supported
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getCategories() throws TranslationException {
        // Return empty map instead of null - AEM 6.5 expects non-null
        // Empty map indicates default category behavior
        return Collections.emptyMap();
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
        return previewPath != null ? previewPath : "";
    }

    /**
     * Get the config resource path.
     */
    public String getConfigResourcePath() {
        return resource != null ? resource.getPath() : "";
    }
}