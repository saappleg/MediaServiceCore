package com.liskovsoft.youtubeapi.videoinfo.models;

/**
 * Minimal Gson model for the optional VISIONOS player response.
 *
 * Keeping this model deliberately small avoids running the large response through the legacy
 * recursive JsonPath adapter just to extract three scalar values.
 */
public class VideoInfoHlsGson {
    private StreamingData streamingData;
    private Storyboards storyboards;

    public String getHlsManifestUrl() {
        return streamingData != null ? streamingData.hlsManifestUrl : null;
    }

    public String getDashManifestUrl() {
        return streamingData != null ? streamingData.dashManifestUrl : null;
    }

    public String getStoryboardSpec() {
        return storyboards != null && storyboards.playerStoryboardSpecRenderer != null
                ? storyboards.playerStoryboardSpecRenderer.spec : null;
    }

    private static class StreamingData {
        private String hlsManifestUrl;
        private String dashManifestUrl;
    }

    private static class Storyboards {
        private PlayerStoryboardSpecRenderer playerStoryboardSpecRenderer;
    }

    private static class PlayerStoryboardSpecRenderer {
        private String spec;
    }
}
