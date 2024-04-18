package org.utl.conversion_de_moneda.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BancoService {
    @GET("series/{serie}/datos/oportuno")
    Call<ResponseBody> obtenerTipoCambio(@Path("serie") String serie, @Query("token")String token);
}
