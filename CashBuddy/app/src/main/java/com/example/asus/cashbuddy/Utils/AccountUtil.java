package com.example.asus.cashbuddy.Utils;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.example.asus.cashbuddy.Model.Merchant;
import com.example.asus.cashbuddy.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.regex.Pattern;

public class AccountUtil {

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private static User currentUser;
    private static Merchant currentMerchant;

    public static com.example.asus.cashbuddy.Model.User getCurrentUser() {
        return currentUser;
    }

    public static Merchant getCurrentMerchant() {
        return currentMerchant;
    }

    public static void setCurrentAccount(User currentUser) {
        AccountUtil.currentUser = currentUser;
        AccountUtil.currentMerchant = null;
    }

    public static void setCurrentAccount(Merchant currentMerchant) {
        AccountUtil.currentMerchant = currentMerchant;
        AccountUtil.currentUser = null;
    }

    public static Task<Void> createUserOtherInformation(String name, String phoneNumber, Uri profilePictureUri, int balance) {
        return updateUserOtherInformation(name, phoneNumber, profilePictureUri, balance).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                return createRole("USER");
            }
        }).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                return createMappingPhoneNumberToUid();
            }
        });
    }

    public static Task<Void> createMerchantOtherInformation(final String name, final String phoneNumber,
                                                         final String email, final String location, final int balance) {
        return updateMerchantOtherInformation(name, phoneNumber, email, location, balance)
                .continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                        return createRole("UNVERIFIED_MERCHANT");
                    }
                }).continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                        return createMappingPhoneNumberToUid();
                    }
                });
    }

    public static Task<Void> updateUserOtherInformation(final String name, final String email, Uri profilePictureUri, final int balance) {
        if(profilePictureUri == null) {
            return updateUserInformationOnDatabase(name, email, null, balance);
        }
        else return uploadProfilePicture(profilePictureUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                String url = task.getResult().getDownloadUrl().toString();

                return updateUserInformationOnDatabase(name, email, url, balance);
            }
        });
    }

    public static Task<Void> updateUserOtherInformation(final User user, Uri profilePictureUri) {
        if(profilePictureUri == null) {
            return updateUserInformationOnDatabase(user);
        }
        else return uploadProfilePicture(profilePictureUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                String url = task.getResult().getDownloadUrl().toString();
                user.setProfilePictureUrl(url);
                return updateUserInformationOnDatabase(user);
            }
        });
    }

    public static Task<Void> updateMerchantOtherInformation(final String name, final String phoneNumber, final String email, final String location, final int balance) {
        return updateMerchantInformationOnDatabase(name, phoneNumber, email, location, balance);
    }
    public static Task<Void> updateMerchantOtherInformation(final Merchant merchant) {
        return updateMerchantInformationOnDatabase(merchant);
    }

    private static Task<Void> updateUserInformationOnDatabase(String name, String email, String profilePictureUrl, int balance) {
        String device_token = FirebaseInstanceId.getInstance().getToken();
        return updateUserInformationOnDatabase(new User(name, email, profilePictureUrl, device_token, balance));
    }

    private static Task<Void> updateUserInformationOnDatabase(User user) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference("users");

        return reference.child(firebaseUser.getUid()).setValue(user);
    }

    private static Task<Void> updateMerchantInformationOnDatabase(String name, String phoneNumber, String email, String location, int balance) {
        String device_token = FirebaseInstanceId.getInstance().getToken();
        return updateMerchantInformationOnDatabase(new Merchant(name, phoneNumber, email, location, device_token, balance));
    }

    private static Task<Void> updateMerchantInformationOnDatabase(Merchant merchant) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference("merchant");

        return reference.child(firebaseUser.getUid()).setValue(merchant);
    }

    private static UploadTask uploadProfilePicture(Uri profilePictureUri) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference directoryReference = storageReference.child("profilepics");
        StorageReference profilePictureReference = directoryReference.child(firebaseUser.getUid() + ".jpg");

        if (profilePictureUri != null){
            return profilePictureReference.putFile(profilePictureUri);
        }
        else return null;
    }

    private static Task<Void> createRole(String role) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference("role");

        return reference.child(firebaseUser.getUid()).setValue(role);
    }

    private static Task<Void> createMappingPhoneNumberToUid() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference("phonenumbertouid");

        return reference.child(firebaseUser.getPhoneNumber().replaceAll(Pattern.quote("."), ",")).setValue(firebaseUser.getUid());
    }
}