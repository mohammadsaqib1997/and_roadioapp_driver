package com.roadioapp.roadioapp.mObjects;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.roadioapp.roadioapp.R;
import com.roadioapp.roadioapp.mInterfaces.DBCallbacks;
import com.roadioapp.roadioapp.mModels.UserInfo;
import com.roadioapp.roadioapp.transforms.CircleTransform;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileImageUpdateObject {

    private Activity activity;
    private ButtonEffects btnEffects;
    private ProgressBarObject progressBarObj;
    private FirebaseAuth mAuth;
    private StorageReference mProfileImageRef;
    private PermissionCheckObj permissionCheckObj;

    public final int REQUEST_IMAGE_CAPTURE = 1;
    public final int REQUEST_IMAGE_PICK = 2;

    private String mCurrentPhotoPath;
    private byte[] profileImageBytes;
    private ImageView previewImage;

    public ProfileImageUpdateObject(Activity activity) {
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
        mProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile_images");
        btnEffects = new ButtonEffects(activity);
        progressBarObj = new ProgressBarObject(activity);
        permissionCheckObj = new PermissionCheckObj(activity);
    }

    public void showUpdateProfileImgDialog(){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.upd_profile_img_dialog);

        previewImage = (ImageView) dialog.findViewById(R.id.userProfileImg);

        LinearLayout chgBtn = (LinearLayout) dialog.findViewById(R.id.changeBtn);
        btnEffects.btnEventEffRounded(chgBtn);
        LinearLayout subBtn = (LinearLayout) dialog.findViewById(R.id.subBtn);
        btnEffects.btnEventEffRounded(subBtn);

        mProfileImageRef.child(mAuth.getCurrentUser().getUid()+".jpg").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(!task.isSuccessful()){
                    progressBarObj.hideProgressDialog();
                }else{
                    Picasso.with(activity).load(task.getResult()).placeholder(R.drawable.circle_img).transform(new CircleTransform()).into(previewImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBarObj.hideProgressDialog();
                        }

                        @Override
                        public void onError() {
                            progressBarObj.hideProgressDialog();
                        }
                    });
                }
            }
        });

        chgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageDialog();
            }
        });

        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(profileImageBytes != null){
                    progressBarObj.showProgressDialog();
                    UploadTask uploadTask = mProfileImageRef.child(mAuth.getCurrentUser().getUid()+".jpg").putBytes(profileImageBytes);
                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            progressBarObj.hideProgressDialog();
                            if(!task.isSuccessful()){
                                Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }else{
                                dialog.dismiss();
                                Toast.makeText(activity, "Profile Image has been uploaded!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(activity, "No image selected!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();
        progressBarObj.showProgressDialog();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                profileImageBytes = null;
            }
        });
    }

    private void showImageDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Take a picture of your parcel or select!");
        builder.setPositiveButton("Take Picture", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {

                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        Log.e("CaptureImageErr", ex.getMessage());
                    }

                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(activity,
                                "com.roadioapp.roadioapp.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });
        builder.setNegativeButton("Select Gallery Image", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(!permissionCheckObj.storagePermissionCheck()){
                    permissionCheckObj.setStoragePermission();
                }else{
                    openGallery();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Image"), REQUEST_IMAGE_PICK);
    }

    public void imageToBytes(){
        if(mCurrentPhotoPath != null && !mCurrentPhotoPath.isEmpty()){
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap imageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            genBitmapToSetValues(imageBitmap);
        }
    }

    public void selImageToBytes(Uri selFileURI){
        try {
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), selFileURI);
            genBitmapToSetValues(imageBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void genBitmapToSetValues(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        profileImageBytes = stream.toByteArray();
        previewImage.setImageBitmap(bitmapCircle(bitmap));
    }

    private Bitmap bitmapCircle(Bitmap source){
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        squaredBitmap.recycle();
        return bitmap;
    }
}