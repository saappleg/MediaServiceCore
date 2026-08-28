package com.liskovsoft.googlecommon.common.converters.jsonpath.converter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.liskovsoft.googlecommon.common.converters.jsonpath.typeadapter.GsonTreeTypeAdapter;
import com.liskovsoft.googlecommon.common.converters.jsonpath.typeadapter.JsonPathTypeAdapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/** Fast player-response converter with the established JsonPath adapter as a safe fallback. */
public final class FastJsonPathConverterFactory extends Converter.Factory {
    private final ParseContext mFallbackParser;

    public static FastJsonPathConverterFactory create() {
        Configuration configuration = Configuration.builder()
                .mappingProvider(new GsonMappingProvider())
                .jsonProvider(new GsonJsonProvider())
                .build();
        return new FastJsonPathConverterFactory(JsonPath.using(configuration));
    }

    private FastJsonPathConverterFactory(ParseContext fallbackParser) {
        mFallbackParser = fallbackParser;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
            @NonNull Type type, @Nullable Annotation[] annotations, @Nullable Retrofit retrofit) {
        return new FastJsonPathResponseBodyConverter<>(
                new GsonTreeTypeAdapter<>(type), new JsonPathTypeAdapter<>(mFallbackParser, type));
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(
            @NonNull Type type,
            @Nullable Annotation[] parameterAnnotations,
            @Nullable Annotation[] methodAnnotations,
            @Nullable Retrofit retrofit) {
        return new JsonPathRequestBodyConverter<>(new JsonPathTypeAdapter<>(mFallbackParser, type));
    }
}
