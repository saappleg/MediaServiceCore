package com.liskovsoft.googlecommon.common.converters.jsonpath.typeadapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.liskovsoft.youtubeapi.videoinfo.models.VideoInfo;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class JsonPathTypeAdapterTest {
    @Test
    public void readsNestedPlayerResponseWithoutReparsingChildren() {
        String json = "{"
                + "\"streamingData\":{"
                + "\"formats\":[{\"itag\":18,\"url\":\"https://video.test/18\",\"mimeType\":\"video/mp4\"}],"
                + "\"adaptiveFormats\":[{\"itag\":137,\"url\":\"https://video.test/137\","
                + "\"mimeType\":\"video/mp4\",\"width\":1920,\"height\":1080,"
                + "\"indexRange\":{\"start\":\"100\",\"end\":\"200\"},"
                + "\"initRange\":{\"start\":\"0\",\"end\":\"99\"}}]},"
                + "\"videoDetails\":{\"videoId\":\"fixture-id\",\"title\":\"Fixture\","
                + "\"lengthSeconds\":\"120\",\"isLive\":false},"
                + "\"playabilityStatus\":{\"status\":\"OK\",\"playableInEmbed\":true},"
                + "\"captions\":{\"playerCaptionsTracklistRenderer\":{"
                + "\"captionTracks\":[{\"baseUrl\":\"https://captions.test\","
                + "\"languageCode\":\"en\",\"name\":{\"simpleText\":\"English\"}}],"
                + "\"translationLanguages\":[{\"languageCode\":\"es\","
                + "\"languageName\":{\"simpleText\":\"Spanish\"}}]}},"
                + "\"storyboards\":{\"playerStoryboardSpecRenderer\":{\"spec\":\"storyboard-spec\"}}"
                + "}";

        Configuration configuration = Configuration.builder()
                .mappingProvider(new GsonMappingProvider())
                .jsonProvider(new GsonJsonProvider())
                .build();
        JsonPathTypeAdapter<VideoInfo> adapter = new JsonPathTypeAdapter<>(
                JsonPath.using(configuration), VideoInfo.class);

        VideoInfo legacyResult = adapter.read(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        VideoInfo result = new GsonTreeTypeAdapter<VideoInfo>(VideoInfo.class).read(json);

        assertNotNull(legacyResult);
        assertNotNull(result);
        assertFalse(result.isUnplayable());
        assertEquals("fixture-id", result.getVideoDetails().getVideoId());
        assertEquals(1, result.getRegularFormats().size());
        assertEquals(18, result.getRegularFormats().get(0).getITag());
        assertEquals(1, result.getAdaptiveFormats().size());
        assertEquals("100-200", result.getAdaptiveFormats().get(0).getIndex());
        assertEquals("0-99", result.getAdaptiveFormats().get(0).getInit());
        assertEquals("en", result.getCaptionTracks().get(0).getLanguageCode());
        assertEquals("es", result.getTranslationLanguages().get(0).getLanguageCode());
        assertEquals("storyboard-spec", result.getStoryboardSpec());
    }
}
