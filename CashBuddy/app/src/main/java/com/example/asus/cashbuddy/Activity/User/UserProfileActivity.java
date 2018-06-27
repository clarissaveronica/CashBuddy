package com.example.asus.cashbuddy.Activity.User;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.asus.cashbuddy.Activity.All.ChangePhoneNumberActivity;
import com.example.asus.cashbuddy.Model.User;
import com.example.asus.cashbuddy.Others.EditCancellationDialogFragment;
import com.example.asus.cashbuddy.R;
import com.example.asus.cashbuddy.Utils.AccountUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.asus.cashbuddy.Utils.ExternalStoragePermissionUtil.checkReadExternalStoragePermission;
import static com.example.asus.cashbuddy.Utils.ExternalStoragePermissionUtil.requestReadExternalStoragePermission;

public class UserProfileActivity extends AppCompatActivity implements EditCancellationDialogFragment.CancellationHandler{

    private TextInputEditText name, email;
    private TextView phone;
    private User user;
    private Button editButton, submitButton, changePicture;
    private ImageView profilePictureImageView;
    private ViewGroup loading;

    // Boolean for edited
    private boolean isProfilePictureEdited;

    // Profile picture URI
    private Uri profilePictureUri;

    // Request permission code
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 100;

    // Choose image code
    private static final int CHOOSE_PICTURE_FROM_GALLERY_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Custom Action Bar's Title
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_layout);
        TextView textViewTitle = findViewById(R.id.title);
        textViewTitle.setText(R.string.editProfileTitle);

        isProfilePictureEdited = false;

        //Initialize views
        name = findViewById(R.id.userName);
        email = findViewById(R.id.userEmail);
        phone = findViewById(R.id.phoneNumber);
        editButton = findViewById(R.id.editButton);
        submitButton = findViewById(R.id.submitButton);
        changePicture = findViewById(R.id.picture_button);
        profilePictureImageView = findViewById(R.id.profilepic);
        loading = findViewById(R.id.loadingPanel);
        loading.setVisibility(View.INVISIBLE);

        user = AccountUtil.getCurrentUser();

        //Set text
        name.setText(user.getName());
        email.setText(user.getEmail());
        phone.setText(user.getPhoneNumber());

        //Set image
        if(user.getProfilePictureUrl() != null) {
            Glide.with(this)
                    .load(user.getProfilePictureUrl())
                    .into(profilePictureImageView);
        }

        //Click listener
        submitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                saveChanges();
            }
        });

        changePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePictureFromGallery();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(UserProfileActivity.this, ChangePhoneNumberActivity.class);
                intent.putExtra("role", "user");
                startActivity(intent);
            }
        });

        name.addTextChangedListener(generalTextWatcher);
        email.addTextChangedListener(generalTextWatcher);
    }

    private void saveChanges() {
        // Update information //
        String nameUser = name.getText().toString();
        String emailUser = email.getText().toString();

        user.setName(nameUser);
        user.setEmail(emailUser);

        Task<Void> updateTask;
        if(isProfilePictureEdited) {
            loading.setVisibility(View.VISIBLE);
            updateTask = AccountUtil.updateUserOtherInformation(user, profilePictureUri);
        }
        else {
            updateTask = AccountUtil.updateUserOtherInformation(user, null);
        }

        updateTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Check status
                if(task.isSuccessful()) {
                    loading.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Profile saved", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    loading.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Failed to save", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private TextWatcher generalTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!email.getText().toString().equals("") && !name.getText().toString().equals("")) {
                if (!user.getEmail().equals(email.getText().toString()) || !user.getName().equals(name.getText().toString())) {
                    if (isEmailValid(email.getText().toString())) {
                        submitButton.setAlpha(1);
                        submitButton.setEnabled(true);
                    } else {
                        email.setError("Invalid email");
                        submitButton.setAlpha(0.5f);
                        submitButton.setEnabled(false);
                    }
                } else {
                    submitButton.setAlpha(0.5f);
                    submitButton.setEnabled(false);
                }
            }else{
                if(email.getText().toString().equals("")) email.setError("Email is required");
                if(name.getText().toString().equals("")) name.setError("Name is required");
                submitButton.setAlpha(0.5f);
                submitButton.setEnabled(false);
            }
        }

    };

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void choosePictureFromGallery(){
        if(checkReadExternalStoragePermission(this)) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, CHOOSE_PICTURE_FROM_GALLERY_CODE);
        }
        else {
            requestReadExternalStoragePermission(this, REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    private void changeProfilePicture() {
        if(profilePictureUri == null) {
            profilePictureImageView.setImageResource(R.drawable.logo);
        }else {
            profilePictureImageView.setImageURI(profilePictureUri);
        }
    }

    private void showCancellationConfirmation() {
        if(!isEdited()) {
            finish();
            return;
        }

        DialogFragment dialogFragment = new EditCancellationDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), null);
    }

    private boolean isEdited() {
        String userName = name.getText().toString();
        String userEmail = email.getText().toString();
        return !(userName.equals(user.getName()) && userEmail.equals(user.getEmail()) && !isProfilePictureEdited);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePictureFromGallery();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == CHOOSE_PICTURE_FROM_GALLERY_CODE) {
                isProfilePictureEdited = true;
                profilePictureUri = data.getData();
                changeProfilePicture();
                submitButton.setAlpha(1);
                submitButton.setEnabled(true);
            }
        }
    }

    @Override
    public void resumeRegistration() {}

    @Override
    public void cancelRegistration() {
        finish();
    }

    @Override
    public void onBackPressed() {
        showCancellationConfirmation();
    }

}
