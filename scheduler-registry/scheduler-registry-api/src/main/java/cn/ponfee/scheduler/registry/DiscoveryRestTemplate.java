/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.scheduler.registry;

import cn.ponfee.scheduler.common.base.model.Result;
import cn.ponfee.scheduler.common.util.Collects;
import cn.ponfee.scheduler.common.util.Jsons;
import cn.ponfee.scheduler.core.base.Server;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.math.IntMath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Retry rest template(Method pattern)
 *
 * @param <D> the discovery type
 * @author Ponfee
 */
public class DiscoveryRestTemplate<D extends Server> {

    private final static Logger LOG = LoggerFactory.getLogger(DiscoveryRestTemplate.class);

    public static final Type RESULT_STRING = new ParameterizedTypeReference<Result<String>>() {}.getType();
    public static final Type RESULT_BOOLEAN = new ParameterizedTypeReference<Result<Boolean>>() {}.getType();
    public static final Type RESULT_VOID = new ParameterizedTypeReference<Result<Void>>() {}.getType();
    public static final Object[] EMPTY = new Object[0];
    private static final Set<HttpMethod> QUERY_PARAMS = ImmutableSet.of(
        HttpMethod.GET, HttpMethod.DELETE, HttpMethod.HEAD, HttpMethod.OPTIONS
    );

    private final RestTemplate restTemplate;
    private final Discovery<D> discoveryServer;
    private final int maxRetryTimes;

    private DiscoveryRestTemplate(int connectTimeout,
                                  int readTimeout,
                                  ObjectMapper objectMapper,
                                  Discovery<D> discoveryServer,
                                  int maxRetryTimes) {
        MappingJackson2HttpMessageConverter httpMessageConverter = new MappingJackson2HttpMessageConverter();
        httpMessageConverter.setObjectMapper(objectMapper);
        httpMessageConverter.setSupportedMediaTypes(Collects.concat(httpMessageConverter.getSupportedMediaTypes(), MediaType.TEXT_PLAIN));

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.setMessageConverters(Arrays.asList(
            new ByteArrayHttpMessageConverter(),
            new StringHttpMessageConverter(StandardCharsets.UTF_8),
            new ResourceHttpMessageConverter(),
            new SourceHttpMessageConverter<>(),
            new FormHttpMessageConverter(),
            httpMessageConverter
        ));

        this.restTemplate = restTemplate;
        this.discoveryServer = discoveryServer;
        this.maxRetryTimes = maxRetryTimes;
    }

    public <T> T execute(String path, HttpMethod httpMethod, Type returnType, Object... arguments) throws Exception {
        return doExecute(null, path, httpMethod, returnType, arguments);
    }

    public <T> T execute(String group, String path, HttpMethod httpMethod, Type returnType, Object... arguments) throws Exception {
        return doExecute(group, path, httpMethod, returnType, arguments);
    }

    /**
     * ???returnType=Void.class??????
     * 1?????????????????????????????????http?????????????????????????????????null
     * 2??????????????????????????????http????????????????????????HttpStatusCodeException
     *
     * @param group      the group name
     * @param path       the url path
     * @param httpMethod the http method
     * @param returnType the return type
     * @param arguments  the arguments
     * @param <T>        return type
     * @return invoked remote http response
     * @throws Exception if occur exception
     */
    private <T> T doExecute(String group, String path, HttpMethod httpMethod, Type returnType, Object... arguments) throws Exception {
        List<D> servers = discoveryServer.getDiscoveredServers(group);
        if (CollectionUtils.isEmpty(servers)) {
            throw new IllegalStateException("Not found available " + discoveryServer.discoveryRole().name());
        }

        int serverNumber = servers.size();
        int start = ThreadLocalRandom.current().nextInt(serverNumber);
        for (int i = 0, n = Math.min(serverNumber, maxRetryTimes) + 1; i < n; i++) {
            Server server = servers.get((start + i) % serverNumber);
            String url = String.format("http://%s:%d/%s", server.getHost(), server.getPort(), path);
            try {
                URI uri;
                HttpEntity<?> httpEntity;
                if (QUERY_PARAMS.contains(httpMethod)) {
                    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
                    if (ArrayUtils.isNotEmpty(arguments)) {
                        builder.queryParams(buildQueryParams(arguments));
                    }
                    uri = builder.build().encode().toUri();
                    httpEntity = null;
                } else {
                    uri = restTemplate.getUriTemplateHandler().expand(url, EMPTY);
                    httpEntity = ArrayUtils.isEmpty(arguments) ? null : new HttpEntity<>(arguments);
                }

                RequestCallback requestCallback = restTemplate.httpEntityCallback(httpEntity, returnType);
                ResponseExtractor<ResponseEntity<T>> responseExtractor = restTemplate.responseEntityExtractor(returnType);
                return restTemplate.execute(uri, httpMethod, requestCallback, responseExtractor).getBody();
            } catch (Exception e) {
                if (e instanceof ResourceAccessException || is5xxServerError(e)) {
                    // round-robin retry
                    LOG.error("Invoked http fail, url: " + url + ", req: " + Jsons.toJson(arguments), e);
                    Thread.sleep(300L * IntMath.pow(i + 1, 2));
                } else {
                    LOG.error("Invoked http error, url: " + url + ", req: " + Jsons.toJson(arguments), e);
                    throw e;
                }
            }
        }

        throw new IllegalStateException("Invoke http retried failed: " + path + ", " + Jsons.toJson(arguments));
    }

    public Discovery<D> getDiscoveryServer() {
        return discoveryServer;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    private static MultiValueMap<String, String> buildQueryParams(Object[] arguments) {
        if (ArrayUtils.isEmpty(arguments)) {
            return null;
        }
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>(arguments.length << 1);
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] != null) {
                params.add("args[" + i + "]", Jsons.toJson(arguments[i]));
            }
        }
        return params;
    }

    private static boolean is5xxServerError(Exception e) {
        if (!(e instanceof HttpStatusCodeException)) {
            return false;
        }
        return ((HttpStatusCodeException) e).getStatusCode().is5xxServerError();
    }

    // ----------------------------------------------------------------------------------------builder

    public static <S extends Server> DiscoveryRestTemplateBuilder<S> builder() {
        return new DiscoveryRestTemplateBuilder<>();
    }

    public static class DiscoveryRestTemplateBuilder<S extends Server> {
        private int connectTimeout;
        private int readTimeout;
        private ObjectMapper objectMapper;
        private Discovery<S> discoveryServer;
        private int maxRetryTimes;

        public DiscoveryRestTemplateBuilder<S> connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public DiscoveryRestTemplateBuilder<S> readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public DiscoveryRestTemplateBuilder<S> objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public DiscoveryRestTemplateBuilder<S> discoveryServer(Discovery<S> discoveryServer) {
            this.discoveryServer = discoveryServer;
            return this;
        }

        public DiscoveryRestTemplateBuilder<S> maxRetryTimes(int maxRetryTimes) {
            this.maxRetryTimes = maxRetryTimes;
            return this;
        }

        public DiscoveryRestTemplate<S> build() {
            return new DiscoveryRestTemplate<>(
                connectTimeout, readTimeout, objectMapper, discoveryServer, maxRetryTimes
            );
        }
    }

}
