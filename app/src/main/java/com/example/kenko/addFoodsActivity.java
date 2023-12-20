package com.example.kenko;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class addFoodsActivity extends AppCompatActivity {
    public ArrayList<String> foodArray;
    public ArrayList<FoodItem> itemArray;
    public ArrayAdapter adapter;
    public ListView foodList;

    private URL urlObject;
    private double ENERC_KCAL = 0.0;
    private double PROCNT = 0.0;
    private double FAT = 0.0;
    private double CHOCDF = 0.0;
    private double FIBTG = 0.0;
    private final int SHIFT_AMT = 180;
    private final int SHIFT_AMT_NEG = -180;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_foods);

        FloatingActionButton add_to_list = findViewById(R.id.addToList);
        FloatingActionButton save = findViewById(R.id.save);
        FloatingActionButton camera = findViewById(R.id.camera);
        FloatingActionButton cancel = findViewById(R.id.cancel);
        EditText foodText = findViewById(R.id.addFood);
        EditText qty = findViewById(R.id.qty);
        foodArray = new ArrayList<>();
        itemArray = new ArrayList();
        foodList = findViewById(R.id.food_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, foodArray);
        foodList.setAdapter(adapter);

        SharedPreferences sp = getSharedPreferences("pref_key", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String fromIntent = sp.getString("fromIntent", "false");
        if (fromIntent.equals("false")) {
            String pastData = sp.getString("pastData", "not_exists");
            if (pastData.equals("exists")) {
                String arr = sp.getString("itemArraySize", "not_exists");
                if (!arr.equals("not_exists") && foodArray.size() == 0) {
                    for (int i = 0; i < Integer.parseInt(arr); i++) {
                        String s = Integer.toString(i);
                        String[] itemData = sp.getString("Item"+s, "").split(" ");
                        foodArray.add(sp.getString("Food"+s, "Error"));
                        FoodItem foodItem = new FoodItem(Integer.parseInt(itemData[0]), Double.parseDouble(itemData[1]),
                                Double.parseDouble(itemData[2]), Double.parseDouble(itemData[3]), Double.parseDouble(itemData[4]),
                                Double.parseDouble(itemData[5]));
                        itemArray.add(foodItem);
                        editor.remove("Food"+s);
                        editor.remove("Item"+s);
                    }
                    EditText editTextFood = findViewById(R.id.addFood);
                    EditText editTextQty = findViewById(R.id.qty);
                    FloatingActionButton addFab = findViewById(R.id.addToList);
                    int shift = SHIFT_AMT*foodArray.size();
                    editTextFood.animate().translationYBy(shift);
                    editTextQty.animate().translationYBy(shift);
                    addFab.animate().translationYBy(shift);
                    editor.remove("itemArraySize");
                }
                String food = sp.getString("foodText", "not_exists");
                if (!food.equals("not_exists")) {
                    EditText f = findViewById(R.id.addFood);
                    f.setText(food);
                    editor.remove("foodText");
                }
                String q = sp.getString("qtyText", "not_exists");
                if (!q.equals("not_exists")) {
                    EditText qT = findViewById(R.id.qty);
                    qT.setText(q);
                    editor.remove("qtyText");
                }
                editor.remove("pastData");
                editor.apply();
            }
        }
        else {
            editor.remove("fromIntent");
            editor.apply();
        }

        add_to_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (foodText.getText().toString().equals("") || qty.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid food and quantity.", Toast.LENGTH_SHORT).show();
                }
                else {
                    String baseUrl = "https://api.edamam.com/api/food-database/v2/parser?app_id=8df70d70&app_key=787e102e6eb88078100b13aab9479c1c&ingr=";
                    String footerUrl = "&nutrition-type=logging&category=generic-foods&category=generic-meals&category=packaged-foods&category=fast-foods";
                    String userUrl = foodText.getText().toString().replaceAll(" ", "%20");
                    String url = baseUrl+userUrl+footerUrl;
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Callable<String> callable = new Callable<String>() {
                        @Override
                        public String call() throws IOException {
                            return getAPIResponse(url);
                        }
                    };
                    Future<String> future = executor.submit(callable);
                    executor.shutdown();
                    try {
                        String resultAPI = future.get();
                        int startIndex = resultAPI.indexOf("{");
                        int endIndex = resultAPI.indexOf("food", resultAPI.indexOf("},{")+1);
                        resultAPI = resultAPI.substring(startIndex, endIndex-3);

                        startIndex = resultAPI.indexOf("knownAs");
                        endIndex = resultAPI.indexOf(",", startIndex);
                        String food = resultAPI.substring(startIndex+10, endIndex-1);

                        startIndex = resultAPI.indexOf("label", resultAPI.indexOf("measure"));
                        endIndex = resultAPI.indexOf(",", startIndex);

                        String label = resultAPI.substring(startIndex+8, endIndex-1);
                        String plural = (qty.getText().toString().equals("1")) ? "" : "s";
                        foodArray.add(qty.getText().toString()+" "+label+plural+" of "+food);
                        //keep track of macros, mainly 5 and add to total amount, save to shared preferences when done
                        startIndex = resultAPI.indexOf("ENERC_KCAL")+12;
                        endIndex = resultAPI.indexOf(",", startIndex);
                        String val_kcal = (endIndex-startIndex > 4) ? resultAPI.substring(startIndex, startIndex+4) : resultAPI.substring(startIndex, endIndex);
                        double item_kcal = Double.parseDouble(val_kcal);
                        startIndex = resultAPI.indexOf("PROCNT")+8;
                        endIndex = resultAPI.indexOf(",", startIndex);
                        String val_procnt = (endIndex-startIndex > 4) ? resultAPI.substring(startIndex, startIndex+4) : resultAPI.substring(startIndex, endIndex);
                        double item_procnt = Double.parseDouble(val_procnt);
                        startIndex = resultAPI.indexOf("FAT")+5;
                        endIndex = resultAPI.indexOf(",", startIndex);
                        String val_fat = (endIndex-startIndex > 4) ? resultAPI.substring(startIndex, startIndex+4) : resultAPI.substring(startIndex, endIndex);
                        double item_fat = Double.parseDouble(val_fat);
                        startIndex = resultAPI.indexOf("CHOCDF")+8;
                        endIndex = resultAPI.indexOf(",", startIndex);
                        String val_chocdf = (endIndex-startIndex > 4) ? resultAPI.substring(startIndex, startIndex+4) : resultAPI.substring(startIndex, endIndex);
                        double item_chocdf = Double.parseDouble(val_chocdf);
                        startIndex = resultAPI.indexOf("FIBTG")+7;
                        endIndex = resultAPI.indexOf("}", startIndex);
                        String val_fibtg = (endIndex-startIndex > 4) ? resultAPI.substring(startIndex, startIndex+4) : resultAPI.substring(startIndex, endIndex);
                        double item_fibtg = Double.parseDouble(val_fibtg);
                        int quantity = Integer.parseInt(qty.getText().toString());
                        ENERC_KCAL += quantity*item_kcal;
                        PROCNT += quantity*item_procnt;
                        FAT += quantity*item_fat;
                        CHOCDF += quantity*item_chocdf;
                        FIBTG += quantity*item_fibtg;
                        FoodItem foodItem = new FoodItem(quantity, item_kcal, item_procnt, item_fat, item_chocdf, item_fibtg);
                        itemArray.add(foodItem);

                        adapter.notifyDataSetChanged();
                        EditText foodText = findViewById(R.id.addFood);
                        EditText qtyText = findViewById(R.id.qty);
                        FloatingActionButton addFab = findViewById(R.id.addToList);
                        foodText.getText().clear();
                        qtyText.getText().clear();
                        foodText.clearFocus();
                        qtyText.clearFocus();
                        foodText.animate().translationYBy(SHIFT_AMT);
                        qtyText.animate().translationYBy(SHIFT_AMT);
                        addFab.animate().translationYBy(SHIFT_AMT);
                    }
                    catch (Exception e) {
                        Log.d("Future get() for text input", "FAILED");
                    }
                }
            }
        });
        foodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int posID = (int) id;
                AlertDialog.Builder fooditem_builder = new AlertDialog.Builder(addFoodsActivity.this);
                fooditem_builder.setMessage(itemArray.get(posID).getData())
                        .setCancelable(false)
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("Delete",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                foodArray.remove(posID);
                                itemArray.remove(posID);
                                adapter.notifyDataSetChanged();
                                EditText foodText = findViewById(R.id.addFood);
                                EditText qtyText = findViewById(R.id.qty);
                                FloatingActionButton addFab = findViewById(R.id.addToList);
                                foodText.animate().translationYBy(SHIFT_AMT_NEG);
                                qtyText.animate().translationYBy(SHIFT_AMT_NEG);
                                addFab.animate().translationYBy(SHIFT_AMT_NEG);
                                dialog.cancel();
                            }
                        });
                AlertDialog fooditem_alert = fooditem_builder.create();
                fooditem_alert.setTitle(foodArray.get(posID).toString());
                fooditem_alert.show();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (foodArray.size() > 0) {
                    SharedPreferences sp = getSharedPreferences("pref_key", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("newFoodData", "exists");
                    editor.putString("ENERC_KCAL", Double.toString(ENERC_KCAL));
                    editor.putString("PROCNT", Double.toString(PROCNT));
                    editor.putString("FAT", Double.toString(FAT));
                    editor.putString("CHOCDF", Double.toString(CHOCDF));
                    editor.putString("FIBTG", Double.toString(FIBTG));
                    editor.apply();
                }
                foodArray.clear();
                itemArray.clear();
                finish();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(takePictureIntent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sp = getSharedPreferences("pref_key", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (itemArray.size() > 0) {
            editor.putString("pastData", "exists");
            editor.putString("itemArraySize", Integer.toString(foodArray.size()));
            for (int i = 0; i < itemArray.size(); i++) {
                String s = Integer.toString(i);
                editor.putString("Food"+s, foodArray.get(i));
                editor.putString("Item"+s, itemArray.get(i).getOnlyData());
            }
        }
        EditText foodText = findViewById(R.id.addFood);
        EditText qtyText = findViewById(R.id.qty);
        if (foodText.getText().toString().length() > 0) {
            editor.putString("pastData", "exists");
            editor.putString("foodText", foodText.getText().toString());
        }
        if (qtyText.getText().toString().length() > 0) {
            editor.putString("pastData", "exists");
            editor.putString("qtyText", qtyText.getText().toString());
        }
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("pref_key", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String fromIntent = sp.getString("fromIntent", "false");
        if (fromIntent.equals("false")) {
            String pastData = sp.getString("pastData", "not_exists");
            if (pastData.equals("exists")) {
                String arr = sp.getString("itemArraySize", "not_exists");
                if (!arr.equals("not_exists") && foodArray.size() == 0) {
                    for (int i = 0; i < Integer.parseInt(arr); i++) {
                        String s = Integer.toString(i);
                        String[] itemData = sp.getString("Item"+s, "").split(" ");
                        foodArray.add(sp.getString("Food"+s, "Error"));
                        FoodItem foodItem = new FoodItem(Integer.parseInt(itemData[0]), Double.parseDouble(itemData[1]),
                                Double.parseDouble(itemData[2]), Double.parseDouble(itemData[3]), Double.parseDouble(itemData[4]),
                                Double.parseDouble(itemData[5]));
                        itemArray.add(foodItem);
                        editor.remove("Food"+s);
                        editor.remove("Item"+s);
                    }
                    EditText editTextFood = findViewById(R.id.addFood);
                    EditText editTextQty = findViewById(R.id.qty);
                    FloatingActionButton addFab = findViewById(R.id.addToList);
                    int shift = SHIFT_AMT*foodArray.size();
                    editTextFood.animate().translationYBy(shift);
                    editTextQty.animate().translationYBy(shift);
                    addFab.animate().translationYBy(shift);
                    editor.remove("itemArraySize");
                }
                String food = sp.getString("foodText", "not_exists");
                if (!food.equals("not_exists")) {
                    EditText f = findViewById(R.id.addFood);
                    f.setText(food);
                    editor.remove("foodText");
                }
                String q = sp.getString("qtyText", "not_exists");
                if (!q.equals("not_exists")) {
                    EditText qT = findViewById(R.id.qty);
                    qT.setText(q);
                    editor.remove("qtyText");
                }
                editor.remove("pastData");
            }
        }
        else {
            editor.remove("fromIntent");
        }
        String newBarcodeData = sp.getString("barcodeData", "not_exists");
        if (newBarcodeData.equals("exists")) {
            String baseUrl = "https://api.edamam.com/api/food-database/v2/parser?app_id=8df70d70&app_key=787e102e6eb88078100b13aab9479c1c&upc=";
            String footerUrl = "&nutrition-type=logging&category=generic-foods&category=generic-meals&category=packaged-foods&category=fast-foods";
            String barcode = sp.getString("barcode", "not_exists");
            String url = baseUrl+barcode+footerUrl;
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Callable<String> callable = new Callable<String>() {
                @Override
                public String call() throws IOException {
                    return getAPIResponse(url);
                }
            };
            Future<String> future = executor.submit(callable);
            executor.shutdown();
            try {
                String resultAPI = future.get();
                Log.d("resultAPI", resultAPI);
                int startIndex = resultAPI.indexOf("knownAs");
                int endIndex = resultAPI.indexOf(",", startIndex);
                String food = resultAPI.substring(startIndex+10, endIndex-1);
                foodArray.add("1 serving of "+food);
                //keep track of macros, mainly 5 and add to total amount, save to shared preferences when done
                startIndex = resultAPI.indexOf("ENERC_KCAL")+12;
                endIndex = resultAPI.indexOf(",", startIndex);
                String val_kcal = (endIndex-startIndex > 4) ? resultAPI.substring(startIndex, startIndex+4) : resultAPI.substring(startIndex, endIndex);
                double item_kcal = Double.parseDouble(val_kcal);
                startIndex = resultAPI.indexOf("PROCNT")+8;
                endIndex = resultAPI.indexOf(",", startIndex);
                String val_procnt = (endIndex-startIndex > 4) ? resultAPI.substring(startIndex, startIndex+4) : resultAPI.substring(startIndex, endIndex);
                double item_procnt = Double.parseDouble(val_procnt);
                startIndex = resultAPI.indexOf("FAT")+5;
                endIndex = resultAPI.indexOf(",", startIndex);
                String val_fat = (endIndex-startIndex > 4) ? resultAPI.substring(startIndex, startIndex+4) : resultAPI.substring(startIndex, endIndex);
                double item_fat = Double.parseDouble(val_fat);
                startIndex = resultAPI.indexOf("CHOCDF")+8;
                endIndex = resultAPI.indexOf(",", startIndex);
                String val_chocdf = (endIndex-startIndex > 4) ? resultAPI.substring(startIndex, startIndex+4) : resultAPI.substring(startIndex, endIndex);
                double item_chocdf = Double.parseDouble(val_chocdf);
                startIndex = resultAPI.indexOf("FIBTG")+7;
                endIndex = resultAPI.indexOf(",", startIndex);
                String val_fibtg = (endIndex-startIndex > 4) ? resultAPI.substring(startIndex, startIndex+4) : resultAPI.substring(startIndex, endIndex);
                double item_fibtg = Double.parseDouble(val_fibtg);
                Log.d("Fixed double variables", "Success");
                int quantity = 1;
                ENERC_KCAL += quantity*item_kcal;
                PROCNT += quantity*item_procnt;
                FAT += quantity*item_fat;
                CHOCDF += quantity*item_chocdf;
                FIBTG += quantity*item_fibtg;
                FoodItem foodItem = new FoodItem(quantity, item_kcal, item_procnt, item_fat, item_chocdf, item_fibtg);
                itemArray.add(foodItem);
                adapter.notifyDataSetChanged();
                EditText foodText = findViewById(R.id.addFood);
                EditText qtyText = findViewById(R.id.qty);
                FloatingActionButton addFab = findViewById(R.id.addToList);
                foodText.animate().translationYBy(SHIFT_AMT);
                qtyText.animate().translationYBy(SHIFT_AMT);
                addFab.animate().translationYBy(SHIFT_AMT);
            }
            catch (Exception e) {
                Toast.makeText(this,"An error has occurred. Please try again.", Toast.LENGTH_SHORT).show();
                Log.d("Future get() for camera", "FAILED");
            }
            editor.remove("barcodeData");
            editor.remove("barcode");
        }
        editor.apply();
    }

    public String getAPIResponse(String url) throws IOException {
        String responseString = null;
        String inputLine = "";
        try {
            urlObject = new URL(url);
        }
        catch(Exception e) {
            e.printStackTrace();
            responseString = "0";
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) urlObject.openConnection();
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                // If this log is printed, then something went wrong with your call
                Toast.makeText(getApplicationContext(), "API Call Error", Toast.LENGTH_SHORT).show();
                Log.d("Response from Send", "FAILED");
            } else {
                // Parse the input into a string and then read it
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                // Return response
                return response.toString();
            }
        } catch(Exception e) {
            e.printStackTrace();
            responseString = "0";
        } finally {
            connection.disconnect();
        }

        return responseString;
    }
}