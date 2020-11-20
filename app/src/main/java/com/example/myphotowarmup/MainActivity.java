package com.example.myphotowarmup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.myphotowarmup.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);

    }

    public void cameraBtnPressed(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // ask for some app, to handle the camera
        startActivityForResult(intent, 2); // provide a different number to identify the
        System.out.println("camera kaldet");
    }

    // Save to the shared Photos folder.
    public void saveToPhotosPressed(View view) {
        requestPermissionToExternalStorage();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        // Bitmap resized = Bitmap. createScaledBitmap ( b , 200 , 200 , true ) ;
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "car", "nice");
        //  System.out.println("saved to Photos, width: " + resized.getWidth());
    }


    private static final int REQUEST = 112; // for later use, if you want to handle the event, when

    // the permisison has been granted / rejected, via the onRequestPermissionsResult() method.
    private void requestPermissionToExternalStorage() {
        if (Build.VERSION.SDK_INT >= 23) { // because lower versions don't need the user to give permission
            String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions((Activity) this, PERMISSIONS, REQUEST);
            }
        }
    }

    // String... means: variable number of String objects, turned into an array of Strings
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;  // if it finds any permission, other than PERMISSION_GRANTED, then return false.
                }
            }
        }
        return true;
    }


    // Save to current app directory (not shared)
    public void saveToAppFolder(View view) {
        requestPermissionToExternalStorage();
        Bitmap b = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        try {
            File file = Environment.getExternalStorageDirectory();
            File dir = new File(file.getAbsolutePath() + "/MyImages");
            dir.mkdirs(); // makes any necessary parent directory also
            String fileName = "car.jpg";
            File outFile = new File(dir, fileName);
            FileOutputStream fOut = new FileOutputStream(outFile);
            if (b.compress(Bitmap.CompressFormat.JPEG, 100, fOut)) {
                System.out.println("image gemt i MyImages");
            } else {
                System.out.println("Fejl i image gem i MyImages");
            }
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
        }
    }

    // Load from current app directory
    public void loadPhotoPressed(View view) {
        try {
            File file = Environment.getExternalStorageDirectory();
            File file2 = new File(file.getAbsolutePath() + "/MyImages/car.jpg");
            if (file2.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(file2.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
                System.out.println("file2 exists");
            } else {
                System.out.println("file2 doesnt exists");
            }
        } catch (Exception e) {
        }
    }

    // Drawing text on image:

    public Bitmap drawTextToBitmap(Bitmap image, String gText) {
        Bitmap.Config bitmapConfig = image.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        image = image.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);// new antialised Paint
        paint.setColor(Color.rgb(161, 161, 161));
        paint.setTextSize((int) (20)); // text size in pixels
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE); // text shadow
        canvas.drawText(gText, 10, 100, paint);
        return image;
    }

    Bitmap manipulated;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) { // from camera
            if (resultCode == -1) {  // -1 is code for OK
                Bitmap bitmap = (Bitmap) data.getExtras().get("data"); // ask for data from the incoming intent.
                manipulated = drawTextToBitmap(bitmap, "Hello World!");
                imageView.setImageBitmap(manipulated);
            }
        }
    }


    public void prov(View view) {
        String id = UUID.randomUUID().toString();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child(id);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        manipulated.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        ref.putBytes(baos.toByteArray()).addOnCompleteListener(snap -> {
            System.out.println("ok uplaodet" + snap);
        }).addOnFailureListener(exception -> {
            System.out.println("failed to upload" + exception);
        });
        System.out.println("mums");
    }

}

