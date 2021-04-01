package distinct.digitalsolutions.prabudhh.HelperClasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import distinct.digitalsolutions.prabudhh.Activities.CreateNotification;
import distinct.digitalsolutions.prabudhh.Activities.PaymentActivity;
import distinct.digitalsolutions.prabudhh.Activities.PlayListSongActivity;
import distinct.digitalsolutions.prabudhh.Activities.SingleSongActivity;
import distinct.digitalsolutions.prabudhh.Adapter.CategoryViewRecyclerViewAdapter;
import distinct.digitalsolutions.prabudhh.Adapter.PlayListRecyclerviewAdapter;
import distinct.digitalsolutions.prabudhh.Database.FirebaseDatabaseClass;
import distinct.digitalsolutions.prabudhh.Interfaces.CategoryFirebaseInterface;
import distinct.digitalsolutions.prabudhh.Interfaces.LoginInterface;
import distinct.digitalsolutions.prabudhh.Interfaces.MostPlayedSongInterface;
import distinct.digitalsolutions.prabudhh.Interfaces.NotificationInterface;
import distinct.digitalsolutions.prabudhh.Interfaces.PaymentAlertInterface;
import distinct.digitalsolutions.prabudhh.Interfaces.PlayListInterface;
import distinct.digitalsolutions.prabudhh.Interfaces.SongPostFirebaseInterface;
import distinct.digitalsolutions.prabudhh.Model.CategoryViewModelClass;
import distinct.digitalsolutions.prabudhh.Model.MostlyPlayed;
import distinct.digitalsolutions.prabudhh.R;
import distinct.digitalsolutions.prabudhh.SharedPreference.PlaySongSharedPreference;

public class HomeHelperClass implements LoginInterface {

    private View mHomeHelperRecyclerView;

    private FirebaseDatabaseClass firebaseDatabaseClass;


    private RecyclerView mHomeMostlyPlayedRecyclerView;
    private CategoryViewRecyclerViewAdapter mHomeCategoryAdapter;
    private List<CategoryViewModelClass> mHomeCategoryModelClass = new ArrayList<>();
    private List<MostlyPlayed> mMostlyPlayed = new ArrayList<>();

    private RecyclerView mHomeRecommendedRecyclerView;
    private CategoryViewRecyclerViewAdapter mHomeRecommendedAdapter;
    private List<CategoryViewModelClass> mHomeRecommendedPlayList = new ArrayList<>();



    private Activity mContext;
    private RelativeLayout mHomeProgressBar;
    private ProgressBarClass progressBarClass;

    private TextView mRecommendedHeader, mMostlyPlayedHeader;
    private PaymentAlertInterface paymentAlertInterface;
    private FirebaseDatabaseClass mFirebaseDatabase;
    private CreateNotification createNotification;

    public HomeHelperClass(Activity mContext, LayoutInflater inflater, ViewGroup viewGroup, PaymentAlertInterface paymentAlertInterface) {

        this.paymentAlertInterface = paymentAlertInterface;
        this.mContext = mContext;

        mHomeHelperRecyclerView = inflater.inflate(R.layout.fragment_home, viewGroup, false);

        mFirebaseDatabase = new FirebaseDatabaseClass();
        firebaseDatabaseClass = new FirebaseDatabaseClass();
        progressBarClass = new ProgressBarClass(mContext);

    }

    @Override
    public void initView() {

        mHomeProgressBar = mContext.findViewById(R.id.progress_bar_layout);

        mRecommendedHeader = mContext.findViewById(R.id.recommended_text);
        mMostlyPlayedHeader = mContext.findViewById(R.id.mostly_played_text);

        mHomeMostlyPlayedRecyclerView = mContext.findViewById(R.id.mostly_played_recycler_view);
        mHomeMostlyPlayedRecyclerView.setNestedScrollingEnabled(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL,false);
        mHomeMostlyPlayedRecyclerView.setLayoutManager(layoutManager);

        mHomeRecommendedRecyclerView = mContext.findViewById(R.id.recommended_recycler_view);
        mHomeRecommendedRecyclerView.setNestedScrollingEnabled(false);

        LinearLayoutManager recommendedLayoutManger = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mHomeRecommendedRecyclerView.setLayoutManager(recommendedLayoutManger);

        mRecommendedHeader.setVisibility(View.GONE);
        mMostlyPlayedHeader.setVisibility(View.GONE);

        progressBarClass.setProgressBarVisible(mHomeProgressBar);

        LoadRecommendedPosts();
        LoadCategoryPosts();

    }

    private void LoadRecommendedPosts() {

        mHomeRecommendedPlayList.clear();

        firebaseDatabaseClass.getRecommendedSongs(new CategoryFirebaseInterface() {
            @Override
            public void onSuccess(List<CategoryViewModelClass> categoryViewModelClasses, List<CategoryViewModelClass> allSongDetails) {

                mHomeRecommendedPlayList = categoryViewModelClasses;

                mHomeRecommendedAdapter = new CategoryViewRecyclerViewAdapter("Recommended Play List", mContext, mHomeRecommendedPlayList, paymentAlertInterface);
                mHomeRecommendedRecyclerView.setAdapter(mHomeRecommendedAdapter);
                mHomeRecommendedAdapter.notifyDataSetChanged();

                mRecommendedHeader.setVisibility(View.VISIBLE);

                //  LoadCategoryPosts();

                //progressBarClass.setProgressBarNotVisible(mHomeProgressBar);
            }

            @Override
            public void onFailure(String error) {

                mRecommendedHeader.setVisibility(View.VISIBLE);

            }
        });
    }

    private void LoadCategoryPosts() {

        mHomeCategoryModelClass.clear();

        firebaseDatabaseClass.getMostlyPlayedSongs(new MostPlayedSongInterface() {
            @Override
            public void onSuccess(List<MostlyPlayed> mostlyPlayeds) {

                mMostlyPlayed = mostlyPlayeds;

                Collections.sort(mMostlyPlayed);

                if (mMostlyPlayed.size() > 10) {

                    mMostlyPlayed.subList(11, mMostlyPlayed.size()).clear();
                }

                firebaseDatabaseClass.getFinalMostlyPlayedSongsData(mMostlyPlayed, new CategoryFirebaseInterface() {
                    @Override
                    public void onSuccess(List<CategoryViewModelClass> categoryViewModelClasses, List<CategoryViewModelClass> allSongDetails) {

                        mHomeCategoryModelClass = categoryViewModelClasses;

                        Collections.sort(mHomeCategoryModelClass);

                        mHomeCategoryAdapter = new CategoryViewRecyclerViewAdapter("Mostly Played"
                                , mContext, mHomeCategoryModelClass, paymentAlertInterface);
                        mHomeMostlyPlayedRecyclerView.setAdapter(mHomeCategoryAdapter);
                        mHomeCategoryAdapter.notifyDataSetChanged();

                        mMostlyPlayedHeader.setVisibility(View.VISIBLE);

                        progressBarClass.setProgressBarNotVisible(mHomeProgressBar);

                    }

                    @Override
                    public void onFailure(String error) {

                        mMostlyPlayedHeader.setVisibility(View.VISIBLE);

                        progressBarClass.setProgressBarNotVisible(mHomeProgressBar);

                    }
                });
            }

            @Override
            public void onFailure(String failed) {

                mMostlyPlayedHeader.setVisibility(View.VISIBLE);

                progressBarClass.setProgressBarNotVisible(mHomeProgressBar);

            }
        });

    }

    @Override
    public View getRootView() {
        return mHomeHelperRecyclerView;
    }

    public void showAlertDialog(CategoryViewModelClass categoryViewModelClass, String categoryName, List<CategoryViewModelClass> categoryViewModelClasses
                                //, String mCategoryid
    ) {

        mFirebaseDatabase.checkUserPaymentStatus(new SongPostFirebaseInterface() {
            @Override
            public void onSuccess(String success, String date, String expiryDate) {

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

                Date strDate = null;

                try {

                    strDate = sdf.parse(expiryDate);

                } catch (ParseException e) {

                    e.printStackTrace();

                }

                if (new Date().after(strDate)) {


                    new AlertDialog.Builder(mContext)
                            .setTitle("Subscription Expired")
                            .setMessage("You need to renewal subscription plan.")
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                                // Continue with delete operation
                                Intent alertIntent = new Intent(mContext, PaymentActivity.class);
                                alertIntent.putExtra("category_name", categoryName);
                                //alertIntent.putExtra("category_id", mCategoryid);
                                mContext.startActivity(alertIntent);

                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(R.mipmap.music_icon)
                            .show();

                    return;

                }

//                if (TextUtils.isEmpty(mCategoryid) || mCategoryid == null) {
//
//                    Intent intent = new Intent(mContext, MainActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    mContext.startActivity(intent);
//                    return;
//
//                }
//
                Intent playListIntent = new Intent(mContext, PlayListSongActivity.class);
                playListIntent.putExtra("song_details", new Gson().toJson(categoryViewModelClass));
                playListIntent.putExtra("category_name", categoryName);
                playListIntent.putExtra("back_button", 1);
                playListIntent.putExtra("all_songs", new Gson().toJson(categoryViewModelClasses));
                mContext.startActivity(playListIntent);
                mContext.overridePendingTransition(0, 0);

            }

            @Override
            public void onFailure(String error) {

                new AlertDialog.Builder(mContext)
                        .setTitle("Paid Content")
                        .setMessage("You need to subscribe to view this content.")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {

                            // Continue with delete operation
                            Intent alertIntent = new Intent(mContext, PaymentActivity.class);
                            alertIntent.putExtra("category_name", categoryName);
                            //alertIntent.putExtra("category_id", mCategoryid);
                            mContext.startActivity(alertIntent);

                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(R.mipmap.music_icon)
                        .show();

            }
        });
    }
}
