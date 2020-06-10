package cz.itnetwork.permissions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final int PERMISSION_MAXIMAL_REQUEST = 1;
    final int PERMISSION_MINIMAL_REQUEST = 2;
    final int PERMISSION_MULTIPLE_REQUEST = 3;
    final int SETTINGS_SHOWED = 11;

    TextView labelPermission1, labelCall, labelLocation;
    Button btnPermissionMinimal, btnPermissionMaximal, btnMultiple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        labelPermission1 = findViewById(R.id.labelPermission1);
        labelCall = findViewById(R.id.labelCall);
        labelLocation = findViewById(R.id.labelLocation);
        btnPermissionMinimal = findViewById(R.id.btnPermissionMinimal);
        btnPermissionMaximal = findViewById(R.id.btnPermissionMaximal);
        btnMultiple = findViewById(R.id.btnMultiple);

        btnPermissionMinimal.setOnClickListener(this);
        btnPermissionMaximal.setOnClickListener(this);
        btnMultiple.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAppByPermissionStatus();
    }

    public void setDefinitiveRejection(boolean isDenied) {
        SharedPreferences sp = getSharedPreferences("permissions_app", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("definitive_rejection", isDenied);
        editor.commit();
    }

    public boolean isDefinitiveRejection() {
        SharedPreferences sp = getSharedPreferences("permissions_app", MODE_PRIVATE);
        return sp.getBoolean("definitive_rejection", false);
    }

    // Aktualizace textů a barev labelů informujících o stavu jednotlivých oprávnění
    public void updateAppByPermissionStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Kamera (fotoaparát)
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                labelPermission1.setText(R.string.permission_ok);
                labelPermission1.setTextColor(Color.GREEN);
                setDefinitiveRejection(false);
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                labelPermission1.setText(R.string.no_permissions);
                labelPermission1.setTextColor(Color.RED);
            }

            // Vytáčení tel. hovorů
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                labelCall.setTextColor(Color.GREEN);
            } else {
                labelCall.setTextColor(Color.RED);
            }

            // Přesná poloha zařízení
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                labelLocation.setTextColor(Color.GREEN);
            } else {
                labelLocation.setTextColor(Color.RED);
            }
        }
    }

    // Základní způsob ověření přítomnosti oprávnění k použití fotoaparátu (kamery) zařízení
    public void checkCameraPermissionGrantedMinimal() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_already_granted, Toast.LENGTH_SHORT).show();
            } else {
                // Žádost o zobrazení systémového dialogu s žádostí o udělení oprávnění
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSION_MINIMAL_REQUEST);
            }
        }
    }

    // Rozšířený způsob ověření přítomnosti oprávnění k použití fotoaparátu (kamery) zařízení
    public void checkCameraPermissionGrantedMaximal() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        // Bylo již udělení oprávnění definitivně odepřeno?
        if (isDefinitiveRejection()) {
            showDialogSettings();
            return;
        }

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Udělení oprávnění uděleno již dříve
            Toast.makeText(this, R.string.permission_already_granted, Toast.LENGTH_SHORT).show();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            // Udělení oprávnění již bylo odmítnuto (nezaškrtnuto políčko "Příště se neptat")
            showDialogExplanation();
        } else {
            // Zobrazení informačního doalogu před zobrazením systémové žádosti
            showDialogInfo(Manifest.permission.CAMERA, "Pro použití fotoaparátu bude nutné této aplikaci udělit oprávnění. Pokračovat k udělení oprávnění?");
        }
    }

    // Základní způsob žádosti o udělení oprávnění k vytáčení tel. hovorů a k přesné poloze zařízení
    public boolean checkCallAndLocationPermissionGranted() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasCallPermission = checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
            boolean hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            ArrayList<String> requiredPermissions = new ArrayList<>();

            if (!hasCallPermission) requiredPermissions.add(Manifest.permission.CALL_PHONE);
            if (!hasLocationPermission) requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

            if (requiredPermissions.isEmpty()) {
                Toast.makeText(this, R.string.permission_already_granted, Toast.LENGTH_SHORT).show();
                return true;
            }

            String[] requiredPermissionsAsArray = requiredPermissions.toArray(new String[requiredPermissions.size()]);

            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    requiredPermissionsAsArray,
                    PERMISSION_MULTIPLE_REQUEST);

            return false;
        }

        return true;
    }

    public void showDialogInfo(final String permission, String message) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(R.string.request_permission);

        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (permission.equals(Manifest.permission.CAMERA)) {

                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    PERMISSION_MAXIMAL_REQUEST);
                        } else {
                            // ... jiné postupy dle typu oprávnění
                        }
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    // Zobrazení dialogového okna s vysvětlením důvodu potřeby udělení oprávnění před zobrazením
    // systémového dialogového okna s žádostí a s možností zaškrtnutí políčka "Příště se neptat".
    public void showDialogExplanation() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(R.string.camera);

        alertDialogBuilder
                .setMessage(R.string.permission_explanation)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                PERMISSION_MAXIMAL_REQUEST);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void showDialogSettings() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle(R.string.additional_permission);

        alertDialogBuilder
                .setMessage(R.string.permissions_settings_info)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, SETTINGS_SHOWED);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_MAXIMAL_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Uživatel oprávnění udělil
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                //Vždy když uživatel odmítne udělit oprávnění a NEZAŠKRTNE políčko "Příště se neptat"
            } else {
                //Uživatel OPAKOVANĚ odmítl udělit oprávnění a zaškrtl políčko "Příště se neptat"
                setDefinitiveRejection(true);
            }
        } else if (requestCode == PERMISSION_MINIMAL_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Uživatel oprávnění udělil
                //Provedení akce, která na uděleném oprávnění závisí
            }
        } else if (requestCode == PERMISSION_MULTIPLE_REQUEST) {
            if (grantResults.length == 2) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //Uživatel udělil obě požadovaná oprávnění
                }
            }
        }

        updateAppByPermissionStatus();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPermissionMaximal:
                checkCameraPermissionGrantedMaximal();
                break;
            case R.id.btnPermissionMinimal:
                checkCameraPermissionGrantedMinimal();
                break;
            case R.id.btnMultiple:
                checkCallAndLocationPermissionGranted();
                break;
        }
    }

    //Bude voláno po zavření okna s nastavením aplikace
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            if (requestCode == SETTINGS_SHOWED) {
                updateAppByPermissionStatus();
            }
        }
    }
}
