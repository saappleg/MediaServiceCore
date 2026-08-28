package com.liskovsoft.googlecommon.common.converters.jsonpath.converter;

import com.liskovsoft.googlecommon.common.converters.jsonpath.typeadapter.GsonTreeTypeAdapter;
import com.liskovsoft.googlecommon.common.converters.jsonpath.typeadapter.JsonPathTypeAdapter;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import okhttp3.ResponseBody;
import retrofit2.Converter;

final class FastJsonPathResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final GsonTreeTypeAdapter<T> mFastAdapter;
    private final JsonPathTypeAdapter<T> mFallbackAdapter;

    FastJsonPathResponseBodyConverter(
            GsonTreeTypeAdapter<T> fastAdapter, JsonPathTypeAdapter<T> fallbackAdapter) {
        mFastAdapter = fastAdapter;
        mFallbackAdapter = fallbackAdapter;
    }

    @Override
    public T convert(ResponseBody value) {
        try {
            String payload = value.string();
            T result = mFastAdapter.read(payload);
            if (result != null) {
                return result;
            }
            return mFallbackAdapter.read(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return null;
        } finally {
            value.close();
        }
    }
}
