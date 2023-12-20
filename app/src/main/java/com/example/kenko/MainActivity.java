package com.example.kenko;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private NotificationChannel mNotificationChannel;
    private NotificationManager mNotificationManager;
    private int totalCalories = 0;
    private int totalProtein = 0;
    private int totalFat = 0;
    private int totalCarbs = 0;
    private int totalFiber = 0;
    private double totalProgress = 0;
    private final int SUGGESTED_CALORIES = 2200;
    private final int SUGGESTED_PROTEIN = 70;
    private final int SUGGESTED_FAT = 60;
    private final int SUGGESTED_CARBS = 300;
    private final int SUGGESTED_FIBER = 15;
    private CircularProgressIndicator kcalRing;
    private CircularProgressIndicator proteinRing;
    private CircularProgressIndicator fatRing;
    private CircularProgressIndicator carbRing;
    private CircularProgressIndicator fiberRing;
    private TextView overallPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        kcalRing = findViewById(R.id.ring_kcal);
        proteinRing = findViewById(R.id.ring_protein);
        fatRing = findViewById(R.id.ring_fat);
        carbRing = findViewById(R.id.ring_carb);
        fiberRing = findViewById(R.id.ring_fiber);
        overallPercent = findViewById(R.id.overallPercent);

        SharedPreferences sp = getSharedPreferences("pref_key", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String pastData = sp.getString("pastData", "not_exists");
        if (pastData.equals("exists")) {
            totalCalories = Integer.parseInt(sp.getString("totalCalories", "0"));
            totalProtein = Integer.parseInt(sp.getString("totalProtein", "0"));
            totalFat = Integer.parseInt(sp.getString("totalFat", "0"));
            totalCarbs = Integer.parseInt(sp.getString("totalCarbs", "0"));
            totalFiber = Integer.parseInt(sp.getString("totalFiber", "0"));
            totalProgress = Double.parseDouble(sp.getString("totalProgress", "0"));
            kcalRing.setProgressCompat(totalCalories, true);
            proteinRing.setProgressCompat(totalProtein, true);
            fatRing.setProgressCompat(totalFat, true);
            carbRing.setProgressCompat(totalCarbs, true);
            fiberRing.setProgressCompat(totalFiber, true);
            DecimalFormat df = new DecimalFormat("#.##");
            String dfFormatted = df.format(totalProgress)+"%";
            overallPercent.setText(dfFormatted);
            editor.remove("pastData");
            editor.remove("totalCalories");
            editor.remove("totalProtein");
            editor.remove("totalFat");
            editor.remove("totalCarbs");
            editor.remove("totalFiber");
            editor.remove("totalProgress");
            editor.apply();
        }

        FloatingActionButton addNutrition = findViewById(R.id.addNutrition);
        addNutrition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getSharedPreferences("pref_key", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("fromIntent", "true");
                editor.apply();
                Intent toAddFoodsActivity = new Intent(getApplicationContext(), addFoodsActivity.class);
                startActivity(toAddFoodsActivity);
            }
        });
        FloatingActionButton toExercise = findViewById(R.id.exerciseFab);
        toExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toExerciseActivity = new Intent(getApplicationContext(), ExerciseActivity.class);
                startActivity(toExerciseActivity);
            }
        });
        kcalRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder kcal_builder = new AlertDialog.Builder(MainActivity.this);
                kcal_builder.setMessage("Total Calories Taken: "+totalCalories+"%")
                        .setCancelable(false)
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog kcal_alert = kcal_builder.create();
                kcal_alert.setTitle("Calorie Amount");
                kcal_alert.show();
            }
        });

    }

    public double average(int a, int b, int c, int d, int e) {
        int sum = a+b+c+d+e;
        return ((double)sum)/5;
    }

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 2);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sp = getSharedPreferences("pref_key", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("pastData", "exists");
        editor.putString("totalCalories", Integer.toString(totalCalories));
        editor.putString("totalProtein", Integer.toString(totalProtein));
        editor.putString("totalFat", Integer.toString(totalFat));
        editor.putString("totalCarbs", Integer.toString(totalCarbs));
        editor.putString("totalFiber", Integer.toString(totalFiber));
        editor.putString("totalProgress", Double.toString(totalProgress));
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("pref_key", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        String pastData = sp.getString("pastData", "not_exists");
        if (pastData.equals("exists")) {
            totalCalories = Integer.parseInt(sp.getString("totalCalories", "0"));
            totalProtein = Integer.parseInt(sp.getString("totalProtein", "0"));
            totalFat = Integer.parseInt(sp.getString("totalFat", "0"));
            totalCarbs = Integer.parseInt(sp.getString("totalCarbs", "0"));
            totalFiber = Integer.parseInt(sp.getString("totalFiber", "0"));
            totalProgress = Double.parseDouble(sp.getString("totalProgress", "0"));
            kcalRing.setProgressCompat(totalCalories, true);
            proteinRing.setProgressCompat(totalProtein, true);
            fatRing.setProgressCompat(totalFat, true);
            carbRing.setProgressCompat(totalCarbs, true);
            fiberRing.setProgressCompat(totalFiber, true);
            DecimalFormat df = new DecimalFormat("#.##");
            String dfFormatted = df.format(totalProgress)+"%";
            overallPercent.setText(dfFormatted);
            editor.remove("pastData");
            editor.remove("totalCalories");
            editor.remove("totalProtein");
            editor.remove("totalFat");
            editor.remove("totalCarbs");
            editor.remove("totalFiber");
            editor.remove("totalProgress");
        }
        String newFood = sp.getString("newFoodData", "not_exists");
        if (newFood.equals("exists")) {
            totalCalories += (int)(100*Double.parseDouble(sp.getString("ENERC_KCAL", "0"))/SUGGESTED_CALORIES);
            totalProtein += (int)(100*Double.parseDouble(sp.getString("PROCNT", "0"))/SUGGESTED_PROTEIN);
            totalFat += (int)(100*Double.parseDouble(sp.getString("FAT", "0"))/SUGGESTED_FAT);
            totalCarbs += (int)(100*Double.parseDouble(sp.getString("CHOCDF", "0"))/SUGGESTED_CARBS);
            totalFiber += (int)(100*Double.parseDouble(sp.getString("FIBTG", "0"))/SUGGESTED_FIBER);
            totalCalories = Math.min(100, totalCalories);
            totalProtein = Math.min(100, totalProtein);
            totalFat = Math.min(100, totalFat);
            totalCarbs = Math.min(100, totalCarbs);
            totalFiber = Math.min(100, totalFiber);
            kcalRing.setProgressCompat(totalCalories, true);
            proteinRing.setProgressCompat(totalProtein, true);
            fatRing.setProgressCompat(totalFat, true);
            carbRing.setProgressCompat(totalCarbs, true);
            fiberRing.setProgressCompat(totalFiber, true);
            DecimalFormat df = new DecimalFormat("#.##");
            totalProgress = round(average(totalCalories, totalProtein, totalFat, totalCarbs, totalFiber), 2);
            String dfFormatted = df.format(totalProgress)+"%";
            overallPercent.setText(dfFormatted);
            editor.remove("newFoodData");
            editor.remove("ENERC_KCAL");
            editor.remove("PROCNT");
            editor.remove("FAT");
            editor.remove("CHOCDF");
            editor.remove("FIBTG");
            if (totalCalories == 100) {
                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                String NOTIFICATION_CHANNEL_ID = "4591";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CharSequence name = "goal_channel";
                    String description = "Calories Met";
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    mNotificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
                    mNotificationChannel.setDescription(description);
                    mNotificationManager.createNotificationChannel(mNotificationChannel);
                    Intent notifIntent = new Intent(this, MainActivity.class);
                    notifIntent.setAction(Intent.ACTION_MAIN);
                    notifIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                            notifIntent, PendingIntent.FLAG_IMMUTABLE);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
                    notificationBuilder.setAutoCancel(true)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setContentTitle("Congratulations!")
                            .setContentText("You reached your daily caloric intake!")
                            .setContentIntent(pendingIntent)
                            .setContentInfo("Info");
                    mNotificationManager.notify(1, notificationBuilder.build());
                }
            }
        }
        editor.apply();
    }
}