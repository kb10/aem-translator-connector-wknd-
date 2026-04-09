package com.adobe.aem.guides.wknd.core.translation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.comments.Comment;
import com.adobe.granite.comments.CommentCollection;
import com.adobe.granite.translation.api.TranslationConstants;
import com.adobe.granite.translation.api.TranslationConstants.TranslationMethod;
import com.adobe.granite.translation.api.TranslationConstants.TranslationStatus;
import com.adobe.granite.translation.api.TranslationException;
import com.adobe.granite.translation.api.TranslationMetadata;
import com.adobe.granite.translation.api.TranslationObject;
import com.adobe.granite.translation.api.TranslationResult;
import com.adobe.granite.translation.api.TranslationScope;
import com.adobe.granite.translation.api.TranslationState;
import com.adobe.granite.translation.api.TranslationService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Lufthansa Azure Cognitive Translation Service.
 *
 * Performs actual translation by calling Azure Cognitive Translator API v3.0.
 *
 * Your endpoint format:
 * https://odp-weur-sens-mscgs-ebase-translator-020-n.cognitiveservices.azure.com/translator/text/v3.0/translate
 *
 * API Reference: https://learn.microsoft.com/en-us/azure/ai-services/translator/reference/v3-0-translate
 */
public class AzureCognitiveTranslationService implements TranslationService {

    private static final Logger LOG = LoggerFactory.getLogger(AzureCognitiveTranslationService.class);

    private static final String HEADER_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    private static final String HEADER_SUBSCRIPTION_REGION = "Ocp-Apim-Subscription-Region";
    private static final String HEADER_TRACE_ID = "X-ClientTraceId";

    private final AzureCognitiveTranslationConfig config;
    private final Gson gson = new Gson();
    private String defaultCategory = "general";

    public AzureCognitiveTranslationService(AzureCognitiveTranslationConfig config) {
        this.config = config;
    }

    // ============================================================
    // Core Translation Methods
    // ============================================================

    @Override
    public Map<String, String> supportedLanguages() {
        // Return null to indicate all language pairs are supported
        return null;
    }

    @Override
    public boolean isDirectionSupported(String sourceLanguage, String targetLanguage)
            throws TranslationException {
        // Azure Translator supports all directions
        return true;
    }

    @Override
    public String detectLanguage(String text, TranslationConstants.ContentType contentType)
            throws TranslationException {
        // Language detection not implemented
        LOG.debug("Language detection not implemented");
        return null;
    }

    @Override
    public TranslationResult translateString(String sourceString, String sourceLanguage,
                                             String targetLanguage, TranslationConstants.ContentType contentType, String contentCategory)
            throws TranslationException {

        String traceId = UUID.randomUUID().toString();
        LOG.info("[{}] Translating single string: {} -> {}", traceId, sourceLanguage, targetLanguage);

        if (sourceString == null || sourceString.isEmpty()) {
            return createResult("", sourceLanguage, targetLanguage, sourceString, contentCategory);
        }

        try {
            String fromLang = toAzureLanguageCode(sourceLanguage);
            String toLang = toAzureLanguageCode(targetLanguage);

            List<String> sourceList = new ArrayList<>();
            sourceList.add(sourceString);

            List<String> translated = translateBatch(sourceList, fromLang, toLang, traceId);

            String result = translated.isEmpty() ? sourceString : translated.get(0);
            return createResult(result, sourceLanguage, targetLanguage, sourceString, contentCategory);

        } catch (Exception e) {
            LOG.error("[{}] Translation failed: {}", traceId, e.getMessage(), e);
            throw new TranslationException(e.getMessage(), TranslationException.ErrorCode.GENERAL_EXCEPTION);
        }
    }

    @Override
    public TranslationResult[] translateArray(String[] sourceStrings, String sourceLanguage,
                                              String targetLanguage, TranslationConstants.ContentType contentType, String contentCategory)
            throws TranslationException {

        String traceId = UUID.randomUUID().toString();
        LOG.info("[{}] Translating array: {} strings, {} -> {}",
                traceId, sourceStrings != null ? sourceStrings.length : 0, sourceLanguage, targetLanguage);

        if (sourceStrings == null || sourceStrings.length == 0) {
            return new TranslationResult[0];
        }

        try {
            String fromLang = toAzureLanguageCode(sourceLanguage);
            String toLang = toAzureLanguageCode(targetLanguage);

            List<String> sourceList = new ArrayList<>();
            for (String s : sourceStrings) {
                sourceList.add(s != null ? s : "");
            }

            List<String> translated = translateBatch(sourceList, fromLang, toLang, traceId);

            TranslationResult[] results = new TranslationResult[sourceStrings.length];
            for (int i = 0; i < sourceStrings.length; i++) {
                String translatedText = (i < translated.size()) ? translated.get(i) : sourceStrings[i];
                String sourceText = sourceStrings[i] != null ? sourceStrings[i] : "";
                results[i] = createResult(translatedText, sourceLanguage, targetLanguage,
                        sourceText, contentCategory);
            }

            return results;

        } catch (Exception e) {
            LOG.error("[{}] Translation failed: {}", traceId, e.getMessage(), e);
            throw new TranslationException(e.getMessage(), TranslationException.ErrorCode.GENERAL_EXCEPTION);
        }
    }

    // ============================================================
    // Storage Methods (Not implemented for MT)
    // ============================================================

    @Override
    public TranslationResult[] getAllStoredTranslations(String sourceString, String sourceLanguage,
                                                        String targetLanguage, TranslationConstants.ContentType contentType, String contentCategory,
                                                        String userId, int maxTranslations) throws TranslationException {
        // Not implemented for machine translation
        return new TranslationResult[0];
    }

    @Override
    public void storeTranslation(String sourceString, String sourceLanguage, String targetLanguage,
                                 String translatedString, TranslationConstants.ContentType contentType, String contentCategory,
                                 String userId, int rating, String path) throws TranslationException {
        // Not implemented for machine translation
        LOG.debug("storeTranslation not implemented for MT");
    }

    @Override
    public void storeTranslation(String[] sourceStrings, String sourceLanguage, String targetLanguage,
                                 String[] translatedStrings, TranslationConstants.ContentType contentType, String contentCategory,
                                 String userId, int rating, String path) throws TranslationException {
        // Not implemented for machine translation
        LOG.debug("storeTranslation (array) not implemented for MT");
    }

    // ============================================================
    // Category Methods
    // ============================================================

    @Override
    public String getDefaultCategory() {
        return defaultCategory;
    }

    @Override
    public void setDefaultCategory(String category) {
        this.defaultCategory = category;
    }

    // ============================================================
    // Service Info
    // ============================================================

    @Override
    public TranslationServiceInfo getTranslationServiceInfo() {
        return new TranslationServiceInfo() {
            @Override
            public String getTranslationServiceAttribution() {
                return "Powered by Azure Cognitive Services";
            }

            @Override
            public String getTranslationServiceLabel() {
                return config.providerName();
            }

            @Override
            public String getTranslationServiceName() {
                return "Lufthansa Azure Machine Translation";
            }

            @Override
            public TranslationMethod getSupportedTranslationMethod() {
                return TranslationMethod.MACHINE_TRANSLATION;
            }

            @Override
            public String getServiceCloudConfigRootPath() {
                return "/conf";
            }
        };
    }

    // ============================================================
    // Job Management Methods (Not implemented for synchronous MT)
    // ============================================================

    @Override
    public String createTranslationJob(String name, String description, String sourceLanguage,
                                       String targetLanguage, Date dueDate, TranslationState state, TranslationMetadata metadata)
            throws TranslationException {
        // For synchronous MT, return a generated job ID
        return UUID.randomUUID().toString();
    }

    @Override
    public void updateTranslationJobMetadata(String jobId, TranslationMetadata metadata,
                                             TranslationMethod method) throws TranslationException {
        // Not implemented for synchronous MT
        LOG.debug("updateTranslationJobMetadata not implemented for synchronous MT");
    }

    @Override
    public String uploadTranslationObject(String jobId, TranslationObject translationObject)
            throws TranslationException {
        // Not implemented for synchronous MT
        return UUID.randomUUID().toString();
    }

    @Override
    public TranslationScope getFinalScope(String jobId) throws TranslationException {
        // Not implemented
        return null;
    }

    @Override
    public TranslationStatus updateTranslationJobState(String jobId, TranslationState state)
            throws TranslationException {
        return TranslationStatus.TRANSLATED;
    }

    @Override
    public TranslationStatus getTranslationJobStatus(String jobId) throws TranslationException {
        return TranslationStatus.TRANSLATED;
    }

    @Override
    public CommentCollection<Comment> getTranslationJobCommentCollection(String jobId)
            throws TranslationException {
        return null;
    }

    @Override
    public void addTranslationJobComment(String jobId, Comment comment) throws TranslationException {
        // Not implemented
    }

    @Override
    public InputStream getTranslatedObject(String jobId, TranslationObject translationObject)
            throws TranslationException {
        return null;
    }

    @Override
    public TranslationStatus updateTranslationObjectState(String jobId, TranslationObject translationObject,
                                                          TranslationState state) throws TranslationException {
        return TranslationStatus.TRANSLATED;
    }

    @Override
    public TranslationStatus getTranslationObjectStatus(String jobId, TranslationObject translationObject)
            throws TranslationException {
        return TranslationStatus.TRANSLATED;
    }

    @Override
    public TranslationStatus[] updateTranslationObjectsState(String jobId,
                                                             TranslationObject[] translationObjects, TranslationState[] states) throws TranslationException {
        TranslationStatus[] statuses = new TranslationStatus[translationObjects.length];
        for (int i = 0; i < statuses.length; i++) {
            statuses[i] = TranslationStatus.TRANSLATED;
        }
        return statuses;
    }

    @Override
    public TranslationStatus[] getTranslationObjectsStatus(String jobId,
                                                           TranslationObject[] translationObjects) throws TranslationException {
        TranslationStatus[] statuses = new TranslationStatus[translationObjects.length];
        for (int i = 0; i < statuses.length; i++) {
            statuses[i] = TranslationStatus.TRANSLATED;
        }
        return statuses;
    }

    @Override
    public CommentCollection<Comment> getTranslationObjectCommentCollection(String jobId,
                                                                            TranslationObject translationObject) throws TranslationException {
        return null;
    }

    @Override
    public void addTranslationObjectComment(String jobId, TranslationObject translationObject,
                                            Comment comment) throws TranslationException {
        // Not implemented
    }

    @Override
    public void updateDueDate(String jobId, Date dueDate) throws TranslationException {
        // Not implemented
    }

    // ============================================================
    // Azure Translation Logic
    // ============================================================

    private List<String> translateBatch(List<String> sourceStrings, String fromLang,
                                        String toLang, String traceId) throws TranslationException {

        List<String> allResults = new ArrayList<>();

        int batchStart = 0;
        int batchNum = 1;

        while (batchStart < sourceStrings.size()) {
            List<String> batch = new ArrayList<>();
            int charCount = 0;

            for (int i = batchStart; i < sourceStrings.size() && batch.size() < config.maxItemsPerBatch(); i++) {
                String text = sourceStrings.get(i);
                int textLen = text != null ? text.length() : 0;

                if (charCount + textLen > config.maxCharsPerBatch() && !batch.isEmpty()) {
                    break;
                }

                batch.add(text != null ? text : "");
                charCount += textLen;
            }

            if (batch.isEmpty()) {
                break;
            }

            LOG.debug("[{}] Processing batch {}: {} items, {} chars",
                    traceId, batchNum, batch.size(), charCount);

            List<String> batchResults = translateSingleBatch(batch, fromLang, toLang, traceId);
            allResults.addAll(batchResults);

            batchStart += batch.size();
            batchNum++;
        }

        return allResults;
    }

    private List<String> translateSingleBatch(List<String> batch, String fromLang,
                                              String toLang, String traceId) throws TranslationException {

        List<String> results = new ArrayList<>();

        String url = String.format("%s?api-version=3.0&from=%s&to=%s&textType=%s",
                config.endpoint(), fromLang, toLang, config.textType());

        JsonArray requestBody = new JsonArray();
        for (String text : batch) {
            JsonObject item = new JsonObject();
            item.addProperty("Text", text);
            requestBody.add(item);
        }

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(config.connectTimeoutMs())
                .setSocketTimeout(config.socketTimeoutMs())
                .build();

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {

            HttpPost httpPost = new HttpPost(url);

            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            httpPost.setHeader(HEADER_SUBSCRIPTION_KEY, config.subscriptionKey());
            httpPost.setHeader(HEADER_TRACE_ID, traceId);

            if (config.region() != null && !config.region().isEmpty()) {
                httpPost.setHeader(HEADER_SUBSCRIPTION_REGION, config.region());
            }

            httpPost.setEntity(new StringEntity(gson.toJson(requestBody), StandardCharsets.UTF_8));

            LOG.debug("[{}] Calling Azure Translator", traceId);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                String responseBody = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : "";

                if (statusCode == HttpStatus.SC_OK) {
                    results = parseTranslationResponse(responseBody, batch.size());
                    LOG.debug("[{}] Azure returned {} translations", traceId, results.size());
                } else {
                    LOG.error("[{}] Azure returned HTTP {}: {}", traceId, statusCode, responseBody);
                    throw new TranslationException(
                            "Azure Translator returned HTTP " + statusCode,
                            TranslationException.ErrorCode.GENERAL_EXCEPTION);
                }
            }

        } catch (IOException e) {
            LOG.error("[{}] HTTP error calling Azure: {}", traceId, e.getMessage());
            throw new TranslationException(
                    "HTTP error: " + e.getMessage(),
                    TranslationException.ErrorCode.GENERAL_EXCEPTION);
        }

        return results;
    }

    private List<String> parseTranslationResponse(String responseBody, int expectedCount)
            throws TranslationException {

        List<String> results = new ArrayList<>();

        try {
            JsonArray responseArray = JsonParser.parseString(responseBody).getAsJsonArray();

            for (int i = 0; i < responseArray.size(); i++) {
                JsonObject item = responseArray.get(i).getAsJsonObject();
                JsonArray translations = item.getAsJsonArray("translations");

                if (translations != null && translations.size() > 0) {
                    JsonObject firstTranslation = translations.get(0).getAsJsonObject();
                    String translatedText = firstTranslation.get("text").getAsString();
                    results.add(translatedText);
                } else {
                    results.add("");
                }
            }

        } catch (Exception e) {
            LOG.error("Failed to parse Azure response: {}", e.getMessage());
            throw new TranslationException(
                    "Failed to parse response: " + e.getMessage(),
                    TranslationException.ErrorCode.GENERAL_EXCEPTION);
        }

        return results;
    }

    private String toAzureLanguageCode(String aemLocale) {
        if (aemLocale == null || aemLocale.isEmpty()) {
            return "en";
        }

        String normalized = aemLocale.replace('_', '-').toLowerCase();
        String[] parts = normalized.split("-");
        String lang = parts[0];
        String region = parts.length > 1 ? parts[1].toUpperCase() : "";

        switch (lang) {
            case "zh":
                if ("TW".equals(region) || "HK".equals(region)) {
                    return "zh-Hant";
                }
                return "zh-Hans";
            case "pt":
                if ("BR".equals(region)) {
                    return "pt-br";
                }
                return "pt-pt";
            case "sr":
                return "sr-Latn";
            default:
                return lang;
        }
    }

    private TranslationResult createResult(final String translation, final String sourceLanguage,
                                           final String targetLanguage, final String sourceString, final String category) {

        return new TranslationResult() {

            @Override
            public String getTranslation() {
                return translation;
            }

            @Override
            public String getSourceLanguage() {
                return sourceLanguage;
            }

            @Override
            public String getTargetLanguage() {
                return targetLanguage;
            }

            @Override
            public TranslationConstants.ContentType getContentType() {
                return TranslationConstants.ContentType.HTML;
            }

            @Override
            public String getCategory() {
                return category != null ? category : defaultCategory;
            }

            @Override
            public String getSourceString() {
                return sourceString;
            }

            @Override
            public int getRating() {
                return 0;
            }

            @Override
            public String getUserId() {
                return null;
            }
        };
    }
}
