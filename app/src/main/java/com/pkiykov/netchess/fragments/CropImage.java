package com.pkiykov.netchess.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.pojo.Player;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CropImage extends Fragment {
    public static final String IMAGE_BITMAP = "image bitmap";
    private CropImageView mCropImageView;
    private ProgressDialog pd;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_crop_image, container, false);
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child(Player.AVATARS);
        mCropImageView = (CropImageView) linearLayout.findViewById(R.id.cropImageView);
        Bitmap bitmap = getArguments().getParcelable(IMAGE_BITMAP);
        mCropImageView.setImageBitmap(bitmap);
        mCropImageView.setFixedAspectRatio(true);
        mCropImageView.setScaleX(1.0f);
        mCropImageView.setScaleY(1.0f);
        final Button crop = (Button) linearLayout.findViewById(R.id.crop);
        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crop.setEnabled(false);
                crop.setAlpha(0.5F);
                mCropImageView.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
                    @Override
                    public void onCropImageComplete(CropImageView view, final CropImageView.CropResult result) {
                        pd = new ProgressDialog(getActivity());
                        pd.setMessage(getString(R.string.please_wait));
                        pd.show();
                        Bitmap bitmap1 = result.getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap1.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                        byte[] data = baos.toByteArray();
                        String fileName = "my_chess_avatar";
                        saveToFile(bitmap1, fileName);
                        UploadTask uploadTask;
                        if (mAuth.getCurrentUser() != null) {
                            uploadTask = storageReference.child(mAuth.getCurrentUser().getUid()).putBytes(data);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                                    ((GameActivity) getActivity()).fragmentTransaction(new Profile());
                                    pd.dismiss();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    ((GameActivity) getActivity()).fragmentTransaction(new Profile());
                                    pd.dismiss();
                                }
                            });
                        }else{
                            Toast.makeText(getActivity(), R.string.connection_problem, Toast.LENGTH_SHORT).show();
                            ((GameActivity) getActivity()).fragmentTransaction(new Profile());
                            pd.dismiss();
                        }
                    }
                });
                mCropImageView.getCroppedImageAsync();
            }
        });
        return linearLayout;
    }

    private void saveToFile(Bitmap bmp, String fileName) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileName);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}