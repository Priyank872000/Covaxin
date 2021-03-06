package com.example.covaxin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class Confirmotp extends AppCompatActivity {
    Button btn;
    TextInputEditText tinput;
    Button cerficate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmotp);
        btn = (Button) findViewById(R.id.Cofirm_otp_btn);
        cerficate = (Button) findViewById(R.id.download_cer);
        tinput = (TextInputEditText) findViewById(R.id.confirm_textbox);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validation()) {
                    Log.d("meldi","jay meldi ma ");
                    String confirmOTPurl = "https://cdn-api.co-vin.in/api/v2/auth/public/confirmOTP";
                    HashMap<String, String> params = new HashMap<>();
                    String getValFromOTP = getIntent().getExtras().getString("txid");
                    String mobileno = getIntent().getExtras().getString("mobile");
                    params.put("otp", sha256(tinput.getText().toString()));
                    params.put("txnId", getValFromOTP);
                    JsonObjectRequest CNOTP = new JsonObjectRequest(Request.Method.POST, confirmOTPurl, new JSONObject(params)
                            , new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            String token_no = null;
                            try {
                                token_no = response.get("token").toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            IDproofDatabaseOperation confirmInsert = new IDproofDatabaseOperation(Confirmotp.this);
                            appoinmentclass appoinment = new appoinmentclass(mobileno, token_no);
                            confirmInsert.addUsers(appoinment);
                            Intent in = new Intent(Confirmotp.this, IDProofDetails.class);
                            in.putExtra("Token_no", token_no);
                            Toast.makeText(Confirmotp.this, response.toString(), Toast.LENGTH_SHORT).show();
                            startActivity(in);
                            finish();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(Confirmotp.this, error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("accept", "application/json");
                            params.put("Content-Type", "application/json");
                            return params;
                        }
                    };
                    singletone.getInstance(Confirmotp.this).addToRequestQueue(CNOTP);
                }
            }
        });
    }

    private boolean validation() {
        if (tinput.length() == 0) {
            tinput.setError("Filed is empty");
            return false;
        }
        return true;
    }

    private static String sha256(final String base) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(base.getBytes("UTF-8"));
            final StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                final String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}