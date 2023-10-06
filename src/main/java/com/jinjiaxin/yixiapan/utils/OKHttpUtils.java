package com.jinjiaxin.yixiapan.utils;

import com.jinjiaxin.yixiapan.entity.enums.ResponseCodeEnum;
import com.jinjiaxin.yixiapan.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OKHttpUtils {

    private static final int TIME_OUT_SECONDS = 8;

    private static OkHttpClient.Builder getClientBuilder(){
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().followRedirects(false).retryOnConnectionFailure(false);
        clientBuilder.connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS).readTimeout(TIME_OUT_SECONDS,TimeUnit.SECONDS);
        return clientBuilder;
    }

    private static Request.Builder getRequestBuilder(Map<String, String> header){
        Request.Builder requestBuilder = new Request.Builder();
        if(header != null){
            for(Map.Entry<String, String> map : header.entrySet()){
                String key = map.getKey();
                String value;
                if(map.getValue() == null){
                    value = "";
                }else{
                    value = map.getValue();
                }
                requestBuilder.addHeader(key,value);
            }
        }

        return requestBuilder;
    }

    public static String getRequest(String url){
        ResponseBody responseBody = null;
        try{
            OkHttpClient.Builder clientBuilder = getClientBuilder();
            Request.Builder requestBuilder = getRequestBuilder(null);
            OkHttpClient client = clientBuilder.build();
            Request request = requestBuilder.url(url).build();
            Response response = client.newCall(request).execute();
            responseBody = response.body();
            String responseStr = responseBody.string();
            log.info("postRequest请求地址：{}，返回信息：{}",url,responseStr);
            return responseStr;
        }catch ( SocketTimeoutException | ConnectException e){
            log.error("OKHttp POST 请求超时,url:{}",url,e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        } catch (Exception e) {
            log.error("OKHttp GET 请求异常",e);
            return null;
        }finally {
            if(responseBody != null){
                responseBody.close();
            }
        }
    }

}
