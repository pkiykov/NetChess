package com.pkiykov.netchess.fragments;

import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pkiykov.netchess.GameActivity;
import com.pkiykov.netchess.R;
import com.pkiykov.netchess.others.FirebaseHelper;
import com.pkiykov.netchess.pojo.Player;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class Profile extends Fragment {

    private ImageButton deleteAvatar;
    private Button logoutBtn, uploadPhoto;
    private TextView playerName;
    private ImageView avatar;
    private StorageReference storageRef;
    private FirebaseHelper helper;
    private FirebaseAuth mAuth;
    private String playerId;
    private ListView listView;
    private Player player;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.fragment_profile, container, false);

        storageRef = FirebaseStorage.getInstance().getReference().child(Player.AVATARS);
        mAuth = FirebaseAuth.getInstance();
        if (getArguments() != null) {
            playerId = getArguments().getString(Player.PLAYER_ID);
        }else{
            if(mAuth.getCurrentUser()!= null) {
                playerId = mAuth.getCurrentUser().getUid();
            }
        }
        playerName = (TextView) linearLayout.findViewById(R.id.name);
        listView = (ListView) linearLayout.findViewById(R.id.info_list_view);
        avatar = (ImageView) linearLayout.findViewById(R.id.avatar);

        createListener();

        if (mAuth.getCurrentUser()!= null && mAuth.getCurrentUser().getUid().equals(playerId)) {
            logoutBtn = (Button) linearLayout.findViewById(R.id.logout);
            logoutBtn.setVisibility(View.VISIBLE);
            uploadPhoto = (Button) linearLayout.findViewById(R.id.upload_photo);
            uploadPhoto.setVisibility(View.VISIBLE);
            deleteAvatar = (ImageButton) linearLayout.findViewById(R.id.delete_avatar);
            playerIsNotAvailable();
            playerName.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
            logoutBtn.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
            uploadPhoto.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
            deleteAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    storageRef.child(playerId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getActivity(), R.string.photo_has_been_removed, Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    avatar.setImageResource(R.drawable.empty_avatar);
                    avatar.invalidate();
                    deleteAvatar.setVisibility(View.INVISIBLE);
                }
            });
            playerName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final ViewGroup nullParent = null;
                    final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_new_player_name, nullParent);
                    final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);
                    AlertDialog changeNameDialog = ((GameActivity) getActivity()).createAlertDialog(getActivity(), dialogView, true, getString(R.string.enter_name), ""
                            , getString(R.string.button_done), getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String playerNameNew = edt.getText().toString();
                                    if (!playerNameNew.isEmpty()) {
                                        helper.getRef().child(Player.NAME).setValue(playerNameNew).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getActivity(), R.string.name_has_been_changed, Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        UserProfileChangeRequest userChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(playerNameNew).build();
                                        mAuth.getCurrentUser().updateProfile(userChangeRequest);
                                        playerName.setText(playerNameNew);
                                    } else {
                                        Toast.makeText(getActivity(), R.string.name_can_not_be_empty, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });

                    changeNameDialog.show();
                }
            });
            uploadPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pickImage();
                }
            });
            logoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAuth.signOut();
                    ((GameActivity) getActivity()).fragmentTransaction(new Auth());

                }
            });
        }

        return linearLayout;
    }

    private void playerIsNotAvailable() {
        playerName.setAlpha(0.5F);
        logoutBtn.setAlpha(0.5F);
        uploadPhoto.setAlpha(0.5F);

        playerName.setEnabled(false);
        uploadPhoto.setEnabled(false);
        logoutBtn.setEnabled(false);

        deleteAvatar.setVisibility(View.GONE);

    }

    private void loadPhoto() {
        StorageReference ref = storageRef.child(playerId);
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getActivity()).load(uri).networkPolicy(NetworkPolicy.OFFLINE).fit().into(avatar, new Callback() {
                    @Override
                    public void onSuccess() {
                        if (mAuth.getCurrentUser()!= null && mAuth.getCurrentUser().getUid().equals(playerId)) {
                            deleteAvatar.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError() {
                        storageRef.child(playerId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.with(getActivity()).load(uri).fit().into(avatar);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                avatar.setImageResource(R.drawable.empty_avatar);
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                if (mAuth.getCurrentUser()!= null && mAuth.getCurrentUser().getUid().equals(playerId)) {
                                    deleteAvatar.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                });
            }
        });

    }


    private void createListener() {
        helper = new FirebaseHelper(FirebaseDatabase.getInstance().getReference().child(Player.PLAYERS).child(playerId)
                , new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                player = dataSnapshot.getValue(Player.class);
                if (player != null) {
                    loadPhoto();
                    createPlayer(player);
                    if (mAuth.getCurrentUser()!= null && mAuth.getCurrentUser().getUid().equals(playerId)) {
                        playerIsAvailable();
                    }
                    helper.getRef().removeEventListener(this);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.player_not_exist), Toast.LENGTH_SHORT).show();
                    if (mAuth.getCurrentUser() == null) {
                        ((GameActivity) getActivity()).fragmentTransaction(new Auth());
                    } else {
                        if (mAuth.getCurrentUser().getUid().equals(playerId)) {
                            mAuth.getCurrentUser().delete();
                            helper.getRef().removeValue();
                            storageRef.child(playerId).delete();
                            mAuth.signOut();
                        }
                        ((GameActivity) getActivity()).fragmentTransaction(new RankList());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.toString(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onStop() {
        if (helper != null) {
            helper.getRef().removeEventListener(helper.getValueEventListener());
        }
        super.onStop();
    }

    private void playerIsAvailable() {
        playerName.setEnabled(true);
        playerName.setAlpha(1);
        uploadPhoto.setAlpha(1);
        uploadPhoto.setEnabled(true);
        logoutBtn.setAlpha(1);
        logoutBtn.setEnabled(true);
    }

    private void createPlayer(final Player player) {
        int t = player.getWins() + player.getDraws() + player.getLosses();
        String[] list = {getString(R.string.rating)
                + player.getRating(), getString(R.string.age)
                + player.getAge(), getString(R.string.wins)
                + player.getWins(), getString(R.string.losses)
                + player.getLosses(), getString(R.string.draws)
                + player.getDraws(), getString(R.string.total) + t};
        String name = player.getName();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                bundle.putString(Player.PLAYER_ID, playerId);
                if (i == 0) {
                    Fragment f = new RankList();
                    f.setArguments(bundle);
                    ((GameActivity) getActivity()).fragmentTransaction(f);
                } else if (i == 1) {
                    if (mAuth.getCurrentUser()!= null && mAuth.getCurrentUser().getUid().equals(playerId)) {
                        final Dialog birthdateChangeDialog = new Dialog(getActivity());
                        birthdateChangeDialog.setCancelable(true);
                        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_birthdate, null);
                        final DatePicker picker = (DatePicker) v.findViewById(R.id.date_picker);
                        picker.setMinDate(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(40000));
                        picker.setMaxDate(System.currentTimeMillis());
                        picker.updateDate(2000, 0, 1);
                        Button okBtn = (Button) v.findViewById(R.id.button_ok);
                        okBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int month = picker.getMonth() + 1;
                                String date = picker.getYear() + "-" + String.format("%02d", month) + "-" + String.format("%02d", picker.getDayOfMonth());
                                birthdateChangeDialog.dismiss();
                                player.setBirthdate(date);
                                helper.getRef().child(Player.BIRTHDATE).setValue(date).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getActivity(), getString(R.string.birthdate_has_been_changed), Toast.LENGTH_SHORT).show();
                                        birthdateChangeDialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                                        birthdateChangeDialog.dismiss();
                                    }
                                });
                            }
                        });
                        Button cancelBtn = (Button) v.findViewById(R.id.button_cancel);
                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                birthdateChangeDialog.dismiss();
                            }
                        });
                        birthdateChangeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        birthdateChangeDialog.setContentView(v);
                        birthdateChangeDialog.show();
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.player_birthdate) + player.getBirthdate(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Fragment f = new FinishedGamesList();
                    bundle.putInt(FinishedGamesList.LIST_POSITION, i);
                    f.setArguments(bundle);
                    ((GameActivity) getActivity()).fragmentTransaction(f);
                }
            }
        });
        playerName.setText(name);

    }

    public void pickImage() {
        AlertDialog.Builder getImageFrom = new AlertDialog.Builder(getActivity());
        getImageFrom.setTitle(getString(R.string.chose_or_take_photo));
        final CharSequence[] opsChars = {getResources().getString(R.string.takepic), getResources().getString(R.string.opengallery)};
        getImageFrom.setItems(opsChars, new android.content.DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, which);
                } else if (which == 1) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, which);
                }
                dialog.dismiss();
            }
        });
        AlertDialog bb = getImageFrom.create();

        bb.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Window view = ((AlertDialog) dialogInterface).getWindow();
                assert view != null;
                view.setBackgroundDrawableResource(R.drawable.dialog_background);
            }
        });
        bb.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            Toast.makeText(getActivity(), getString(R.string.image_has_not_been_selected), Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bitmap = createBitmap(data);
        Bundle bundle = new Bundle();
        bundle.putParcelable(CropImage.IMAGE_BITMAP, bitmap);
        Fragment fragment = new CropImage();
        fragment.setArguments(bundle);
        ((GameActivity) getActivity()).fragmentTransaction(fragment);
    }

    private Bitmap createBitmap(Intent data) {
        Bitmap myBitmap = null;
        Uri selectedImage = data.getData();
        try {
            myBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cur = getActivity().getApplicationContext().getContentResolver().query(selectedImage, orientationColumn, null, null, null);
        int orientation = -1;
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
            cur.close();
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        assert myBitmap != null;
        return Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(),
                myBitmap.getHeight(), matrix, true);
    }
}
