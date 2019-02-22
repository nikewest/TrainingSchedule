package ru.alexfitness.trainingschedule.util;

import android.support.annotation.Nullable;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

import ru.alexfitness.trainingschedule.restApi.ApiUrlBuilder;

public class ServiceApiJsonArrayRequest extends JsonArrayRequest {

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "bu1k@";

    public ServiceApiJsonArrayRequest(int method, String url, Response.Listener<JSONArray> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, null, listener, errorListener);
        this.setRetryPolicy(new DefaultRetryPolicy(30 * 1000, 0, 1.0f));
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<>();
        //String credentials = USERNAME + ":" + PASSWORD;
        //String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        headers.put("Authorization", ApiUrlBuilder.getBasicAuthHeader());
        return headers;
    }

}
