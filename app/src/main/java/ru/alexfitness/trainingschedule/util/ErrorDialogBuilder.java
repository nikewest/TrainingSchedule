package ru.alexfitness.trainingschedule.util;

import android.app.AlertDialog;
import android.content.Context;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import java.net.ConnectException;
import ru.alexfitness.trainingschedule.R;

public abstract class ErrorDialogBuilder {

    public static void showDialog(Context context, VolleyError error, AlertDialog.OnDismissListener onDismissListener) {
        String errorText = null;
        if(error.getCause() instanceof ConnectException){
            errorText = context.getString(R.string.cant_connect_to_server);
        } else if(error instanceof TimeoutError){
            errorText = context.getString(R.string.connection_timeout_exceeded);
        } else if (error.networkResponse!=null){
            if(error.networkResponse.statusCode==401){
                errorText = context.getString(R.string.auth_service_failed_msg);
            } else {
                errorText = new String(error.networkResponse.data);
            }
        } else errorText = error.toString();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setMessage(errorText);
        dialogBuilder.setOnDismissListener(onDismissListener);
        dialogBuilder.show();
    }

}
