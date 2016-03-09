package cl.tidchile.antennagpstracker.util;

import java.io.IOException;
import java.util.HashMap;

import cl.tidchile.antennagpstracker.models.Movement;
import cl.tidchile.antennagpstracker.models.Token;
import cl.tidchile.antennagpstracker.util.rest_request_models.PostMovementRequest;
import cl.tidchile.antennagpstracker.util.rest_response_models.PostMovementResponse;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by benjamin on 3/3/16.
 */
public class RestHelper {
    public static Service service;
    public static String token;
    public static final String BASE_URL = "http://emod-zorzal.tidnode.cl:8000/";

    public interface Service {
        @POST("api-auth/token/")
        Call<Token> getUserToken(@Body HashMap<String, String> params);

        @POST("api/movement/{phone}/")
        Call<PostMovementResponse> postMovement(
                @Path("phone") String phone,
                @Body Movement movement);
        @POST("api/movement/{phone}/")
        Call<PostMovementResponse> postMovements(
                @Path("phone") String phone,
                @Body PostMovementRequest movements);
    }

    public static Service getService() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        Interceptor headerInterceptor = new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .header("Authorization", "Token " + token)
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }
        };
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        if (RestHelper.token != null)
            httpClient.addInterceptor(headerInterceptor);
        OkHttpClient client = httpClient.build();
//            //to use realm with retrofit.
//            Gson gson = new GsonBuilder().setExclusionStrategies(new  ExclusionStrategy() {
//                @Override
//                public boolean shouldSkipField(FieldAttributes f) {
//                    return f.getDeclaringClass().equals(RealmObject.class);
//                }
//
//                @Override
//                public boolean shouldSkipClass(Class<?> clazz) {
//                    return false;
//                }
//            }).create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        Service service = retrofit.create(Service.class);
        return service;
    }

    public static void setToken (String token) {
        RestHelper.token = token;
    }
}
