 package com.example.googlepay;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.time.Duration;
import java.util.ArrayList;


 public class MainActivity extends AppCompatActivity {
    EditText amount, note, name, upivirtualid;
    Button send;
    String TAG ="main";
    final int UPI_PAYMENT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        send = (Button) findViewById(R.id.send);
        amount = (EditText)findViewById(R.id.amount_et);
        note = (EditText)findViewById(R.id.note);
        name = (EditText) findViewById(R.id.name);
        upivirtualid =(EditText) findViewById(R.id.upi_id);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Getting the values from the EditTexts
                if (TextUtils.isEmpty(name.getText().toString().trim())){
                    Toast.makeText(MainActivity.this," Name is invalid", Toast.LENGTH_SHORT).show();


                }else if (TextUtils.isEmpty(upivirtualid.getText().toString().trim())){
                    Toast.makeText(MainActivity.this," UPI ID is invalid", Toast.LENGTH_SHORT).show();

                }else if (TextUtils.isEmpty(note.getText().toString().trim())){
                    Toast.makeText(MainActivity.this," Note is invalid", Toast.LENGTH_SHORT).show();

                }else if (TextUtils.isEmpty(amount.getText().toString().trim())){
                    Toast.makeText(MainActivity.this," Amount is invalid", Toast.LENGTH_SHORT).show();
                }else{

                    payUsingUpi(name.getText().toString(), upivirtualid.getText().toString(),
                            note.getText().toString(), amount.getText().toString());

                }


            }
        });
    }


         void payUsingUpi(  String name,String upiId, String note, String amount) {
         Log.e("main ", "name "+name +"--up--"+upiId+"--"+ note+"--"+amount);
         Uri uri = Uri.parse("upi://pay").buildUpon()
                 .appendQueryParameter("pa", upiId)
                 .appendQueryParameter("pn", name)
                 //.appendQueryParameter("mc", "")
                 //.appendQueryParameter("tid", "02125412")
                 //.appendQueryParameter("tr", "25584584")
                 .appendQueryParameter("tn", note)
                 .appendQueryParameter("am", amount)
                 .appendQueryParameter("cu", "INR")
                 //.appendQueryParameter("refUrl", "blueapp")
                 .build();


         Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
         upiPayIntent.setData(uri);

         // will always show a dialog to user to choose an app
         Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

         // check if intent resolves
         if(null != chooser.resolveActivity(getPackageManager())) {
             startActivityForResult(chooser, UPI_PAYMENT);
         } else {
             Toast.makeText(MainActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
         }

     }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         Log.e("main ", "response " + resultCode);
        /*
       E/main: response -1
       E/UPI: onActivityResult: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
       E/UPIPAY: upiPaymentDataOperation: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
       E/UPI: payment successfull: 922118921612
         */
         switch (requestCode) {
             case UPI_PAYMENT:
                 if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                     if (data != null) {
                         String trxt = data.getStringExtra("response");
                         Log.e("UPI", "onActivityResult: " + trxt);
                         ArrayList<String> dataList = new ArrayList<>();
                         dataList.add(trxt);
                         upiPaymentDataOperation(dataList);
                     } else {
                         Log.e("UPI", "onActivityResult: " + "Return data is null");
                         ArrayList<String> dataList = new ArrayList<>();
                         dataList.add("nothing");
                         upiPaymentDataOperation(dataList);
                     }
                 } else {
                     //when user simply back without payment
                     Log.e("UPI", "onActivityResult: " + "Return data is null");
                     ArrayList<String> dataList = new ArrayList<>();
                     dataList.add("nothing");
                     upiPaymentDataOperation(dataList);
                 }
                 break;
         }
     }

     private void upiPaymentDataOperation(ArrayList<String> data) {
         if (isConnectionAvailable(MainActivity.this)) {
             String str = data.get(0);
             Log.e("UPIPAY", "upiPaymentDataOperation: " + str);
             String paymentCancel = "";
             if (str == null) str = "discard";
             String status = "";
             String approvalRefNo = "";
             String response[] = str.split("&");
             for (int i = 0; i < response.length; i++) {
                 String equalStr[] = response[i].split("=");
                 if (equalStr.length >= 2) {
                     if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                         status = equalStr[1].toLowerCase();
                     } else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                         approvalRefNo = equalStr[1];
                     }
                 } else {
                     paymentCancel = "Payment cancelled by user.";
                 }
             }
             if (status.equals("success")) {
                 //Code to handle successful transaction here.
                 Toast.makeText(MainActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                 Log.e("UPI", "payment successfull: "+approvalRefNo);
             }
             else if("Payment cancelled by user.".equals(paymentCancel)) {
                 Toast.makeText(MainActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
                 Log.e("UPI", "Cancelled by user: "+approvalRefNo);

             }
             else {
                 Toast.makeText(MainActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
                 Log.e("UPI", "failed payment: "+approvalRefNo);

             }
         } else {
             Log.e("UPI", "Internet issue: ");

             Toast.makeText(MainActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
         }
     }

     public static boolean isConnectionAvailable(Context context) {
         ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         if (connectivityManager != null) {
             NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
             if (netInfo != null && netInfo.isConnected()
                     && netInfo.isConnectedOrConnecting()
                     && netInfo.isAvailable()) {
                 return true;
             }
         }
         return false;
     }

 }


