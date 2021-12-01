package ir.khalafi.allpermission;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    Button btnSaveInfo, btnShowInfo;
    TextView txtInfo;
    EditText edtInfo;
    String[] permission = {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtInfo = findViewById(R.id.edtInfo);
        txtInfo = findViewById(R.id.txtInfo);
        btnSaveInfo = findViewById(R.id.btnSaveInfo);
        btnShowInfo = findViewById(R.id.btnShowInfo);
        txtInfo.setText(Environment.getExternalStorageDirectory().getAbsolutePath());

        btnSaveInfo.setOnClickListener(v -> {
            if(checkPermission()){
                if (!edtInfo.getText().toString().isEmpty()){
                    saveInfoInFile(edtInfo.getText().toString());
                    Toast.makeText(getApplicationContext(), "Information Inserted", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(), "Enter Information", Toast.LENGTH_SHORT).show();
                }
            }else {
                requestPermission();
            }

        });

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK){
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
                        if (Environment.isExternalStorageManager()){
                            Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        btnShowInfo.setOnClickListener(v -> {
            if (checkPermission()){displaySaveInformation();}else {requestPermission();}
        });
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", new Object[]{getApplicationContext().getPackageName()})));
                activityResultLauncher.launch(intent);
            }catch (Exception e){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activityResultLauncher.launch(intent);
            }
        }else {
            ActivityCompat.requestPermissions(MainActivity.this, permission, 30);
        }
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            return Environment.isExternalStorageManager();
        }else {
            int readCheck = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
            int writeCheck = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
            return readCheck == PackageManager.PERMISSION_GRANTED && writeCheck == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 30) {
            if (grantResults.length > 0) {
                boolean readPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean writePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (writePermission && readPermission) {
                    Toast.makeText(getApplicationContext(), "permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "permission denied", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "you denied permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displaySaveInformation() {
        FileInputStream file;
        String info;
        StringBuilder data = new StringBuilder();
        try {
            File f = new File(Environment.getExternalStoragePublicDirectory("Download"),"info.txt");
            file = new FileInputStream(f);
            InputStreamReader input = new InputStreamReader(file);
            BufferedReader br = new BufferedReader(input);
            while ((info=br.readLine())!=null){
                data.append(info);
            }
            txtInfo.setText(data.toString());
            file.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "File Not find: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }catch (IOException e) {
            Toast.makeText(getApplicationContext(), "IOException: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void saveInfoInFile(String info) {
        FileOutputStream file;
        try {
            File f = new File(Environment.getExternalStoragePublicDirectory("Download"),"info.txt");
            f.createNewFile();
            file = new FileOutputStream(f);
            file.write(info.getBytes());
            file.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "FileNotFoundException: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }catch (IOException e) {
            Toast.makeText(getApplicationContext(), "IOException: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}