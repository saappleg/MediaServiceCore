package com.liskovsoft.youtubeapi.block;

import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import com.liskovsoft.youtubeapi.block.data.SegmentList;
import com.liskovsoft.googlecommon.common.helpers.RetrofitHelper;
import com.liskovsoft.googlecommon.common.helpers.ServiceHelper;
import retrofit2.Call;

import java.util.Set;

public class SponsorBlockService {
    private static final String SUPPORTED_ACTION_TYPES = ServiceHelper.toJsonArrayString("skip", "mute", "poi");
    private static SponsorBlockService sInstance;
    private final SponsorBlockApi mSponsorBlockApi;

    private SponsorBlockService() {
        mSponsorBlockApi = RetrofitHelper.create(SponsorBlockApi.class);
    }

    public static SponsorBlockService instance() {
        if (sInstance == null) {
            sInstance = new SponsorBlockService();
        }

        return sInstance;
    }

    public SegmentList getSegmentList(String videoId) {
        Call<SegmentList> wrapper = isAltServerEnabled() ?
                mSponsorBlockApi.getSegments2WithActions(videoId, SUPPORTED_ACTION_TYPES) :
                mSponsorBlockApi.getSegmentsWithActions(videoId, SUPPORTED_ACTION_TYPES);

        return RetrofitHelper.get(wrapper);
    }

    public SegmentList getSegmentList(String videoId, Set<String> categories) {
        return categories != null && categories.size() > 0 ? getSegmentListInt(videoId, categories) : getSegmentList(videoId);
    }

    private SegmentList getSegmentListInt(String videoId, Set<String> categories) {
        String categoriesJson = ServiceHelper.toJsonArrayString(categories);
        Call<SegmentList> wrapper = isAltServerEnabled() ?
                mSponsorBlockApi.getSegments2(videoId, categoriesJson, SUPPORTED_ACTION_TYPES) :
                mSponsorBlockApi.getSegments(videoId, categoriesJson, SUPPORTED_ACTION_TYPES);

        return RetrofitHelper.get(wrapper);
    }

    private boolean isAltServerEnabled() {
        return GlobalPreferences.isInitialized() && GlobalPreferences.sInstance.isContentBlockAltServerEnabled();
    }
}
