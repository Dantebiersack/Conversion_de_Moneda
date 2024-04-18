package org.utl.conversion_de_moneda;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.JsonParser;
import org.utl.conversion_de_moneda.api.BancoService;
import java.io.IOException;
import java.text.DecimalFormat;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    Button btnConvertir;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch swCambio;
    TextView txtCambio;
    TextView txtConversion;
    EditText txtCantidad;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swCambio = findViewById(R.id.swCambio);
        txtCambio = findViewById(R.id.txtCambio);
        txtConversion = findViewById(R.id.txtConversion);
        txtCantidad = findViewById(R.id.txtCantidad);
        btnConvertir = findViewById(R.id.btnConvertir);

        swCambio.setOnClickListener(view ->{
            if(swCambio.isChecked()){
                txtCambio.setText("Pesos a dólares");
            }else{
                txtCambio.setText("Dólares a pesos");
            }
        });
        btnConvertir.setOnClickListener(view ->{
            realizarSolicitud();
        });
    }

    private void realizarSolicitud() {
        String URL_BASE = "https://www.banxico.org.mx/SieAPIRest/service/v1/";
        String token = "d2d61b63caf464809907166d745f9b6d1f3bc50f5ac4576d26498f91d3b08945";

        // Obtener el valor del EditText
        String cantidad = txtCantidad.getText().toString();

        // Verificar si el valor es válido
        if (!cantidad.isEmpty() && !cantidad.equals("0")) {
            try {
                // Convertir el valor a double
                double cantidadAConvertir = Double.parseDouble(cantidad);
                // Crear la instancia de Retrofit
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(URL_BASE)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                // Crear la interfaz para la API
                BancoService service = retrofit.create(BancoService.class);
                // Realizar la solicitud HTTP
                Call<ResponseBody> call = service.obtenerTipoCambio("SF43718", token);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            // Obtener el tipo de cambio
                            String json = null;
                            try {
                                assert response.body() != null;
                                json = response.body().string();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            // Obtener el campo dato
                            String dato = JsonParser.parseString(json).getAsJsonObject().get("bmx").getAsJsonObject().get("series").getAsJsonArray().get(0).getAsJsonObject().get("datos").getAsJsonArray().get(0).getAsJsonObject().get("dato").getAsString();

                            if(swCambio.isChecked()){//de pesos a dolares
                                if (!dato.isEmpty()){
                                    DecimalFormat df = new DecimalFormat("#.##");
                                    float total = (float) ( cantidadAConvertir / Float.parseFloat(dato));
                                    String msg = "$"+df.format(total);
                                    //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                    txtConversion.setText(msg);
                                } else {
                                    Toast.makeText(MainActivity.this, "Error de api key", Toast.LENGTH_SHORT).show();
                                }
                            }else{//de dolares a pesos
                                if (!dato.isEmpty()){
                                    DecimalFormat df = new DecimalFormat("#.##");
                                    float total = (float) (Float.parseFloat(dato) * cantidadAConvertir);
                                    String msg = "$"+df.format(total);
                                    //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                                    txtConversion.setText(msg);
                                } else {
                                    Toast.makeText(MainActivity.this, "Error de api key", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Error de solicitud", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "La cantidad no puede ser nula o 0", Toast.LENGTH_SHORT).show();
        }
    }
}