package com.liskovsoft.youtubeapi.videoinfo.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.lang.reflect.Field;

public class PlaybackUrlCachingTest {
    @Test
    public void videoUrlCacheIsInvalidatedWhenParametersChange() {
        VideoUrlHolder holder = new VideoUrlHolder(
                "https://video.test/play?n=old&foo=bar", null, null);

        String first = holder.getUrl();
        assertEquals(first, holder.getUrl());

        holder.setNParam("new");
        String updated = holder.getUrl();

        assertTrue(updated.contains("n=new"));
        assertFalse(updated.contains("n=old"));
        assertEquals(updated, holder.getUrl());
    }

    @Test
    public void captionUrlCacheTracksSelectedCaptionFormat() throws Exception {
        CaptionTrack track = new CaptionTrack();
        Field baseUrl = CaptionTrack.class.getDeclaredField("mBaseUrl");
        baseUrl.setAccessible(true);
        baseUrl.set(track, "https://captions.test/timedtext?lang=en");

        CaptionTrack.CaptionFormat originalFormat = CaptionTrack.sFormat;
        try {
            CaptionTrack.sFormat = CaptionTrack.CaptionFormat.VTT;
            String vtt = track.getBaseUrl();
            assertEquals(vtt, track.getBaseUrl());
            assertTrue(vtt.contains("fmt=vtt"));

            CaptionTrack.sFormat = CaptionTrack.CaptionFormat.TTML;
            String ttml = track.getBaseUrl();
            assertTrue(ttml.contains("fmt=ttml"));
            assertFalse(ttml.contains("fmt=vtt"));
            assertEquals(ttml, track.getBaseUrl());
        } finally {
            CaptionTrack.sFormat = originalFormat;
        }
    }
}
