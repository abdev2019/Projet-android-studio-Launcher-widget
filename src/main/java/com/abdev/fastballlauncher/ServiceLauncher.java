package com.abdev.fastballlauncher;


import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.HashMap;
import java.util.List;


public class ServiceLauncher extends Service
{
    private WindowManager windowManager;
    boolean isUpV=false, isUpH=true;
    private View.OnTouchListener onTouchListener;
    private View modal;
    private ImageView btnMove;
    private LinearLayout container;
    private LinearLayout leftContainer;
    private LinearLayout layoutFavourite;
    MyDbAdapter helper;

    private InterstitialAd mInterstitialAd;
    private HashMap<String,LinearLayout> layOrdered;


    @Override
    public void onCreate()
    {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        helper = new MyDbAdapter(this);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId( getString(R.string.admob_interstitial_id) );
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        initMenu();
        initWindowManager();
    }


    private void initWindowManager()
    {
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        onTouchListener = new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            boolean moved=false;

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        moved = false;
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(!moved)
                        {
                            if (isUpH) {
                                slideDown(leftContainer,false);
                                btnMove.setAlpha(0.4f);
                            }
                            else {
                                slideUp(leftContainer,false);
                                btnMove.setAlpha(1f);
                            }
                            isUpH = !isUpH;
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(container, params);
                        moved = true;
                        return true;
                }
                return false;
            }
        };
        btnMove.setOnTouchListener(onTouchListener);
        windowManager.addView(container, params);
    }


    PackageManager manager;
    LinearLayout.LayoutParams MM = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    LinearLayout.LayoutParams sizeAppView = new LinearLayout.LayoutParams(120, 85);
    LinearLayout.LayoutParams sizeAppViewOrd = new LinearLayout.LayoutParams(120, ViewGroup.LayoutParams.WRAP_CONTENT);
    LinearLayout.LayoutParams sizeIcon = new LinearLayout.LayoutParams(60, 60);

    private void initMenu()
    {
        btnMove = new ImageView(this);
        btnMove.setBackgroundResource(R.drawable.pion1);
        btnMove.setLayoutParams(sizeIcon);

        LinearLayout hLayout = new LinearLayout(this);
        hLayout.setOrientation(LinearLayout.HORIZONTAL);
        hLayout.setLayoutParams(MM);
        hLayout.setGravity(Gravity.CENTER);

        final ScrollView sv = new ScrollView(this);

        final ImageView btnHideShow = new ImageView(this);
        btnHideShow.setBackgroundResource(R.drawable.down);
        btnHideShow.setLayoutParams(sizeIcon);
        btnHideShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isUpV) {
                    btnHideShow.setBackgroundResource(R.drawable.down);
                    slideDown(sv, true);
                }
                else {
                    btnHideShow.setBackgroundResource(R.drawable.up);
                    slideUp(sv, true);
                }
                isUpV = !isUpV;
            }
        });
        hLayout.addView(btnHideShow);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setLayoutParams(MM);
        container.setGravity(Gravity.CENTER);

        manager = getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);

        layoutFavourite = new LinearLayout(this);
        layoutFavourite.setOrientation(LinearLayout.HORIZONTAL);
        layoutFavourite.setLayoutParams(MM);
        ImageView star = new ImageView(this);
        star.setImageDrawable(getResources().getDrawable(R.drawable.start));
        star.setLayoutParams(sizeIcon);
        layoutFavourite.addView(star);

        layOrdered = new HashMap<>();
        for(ResolveInfo ri:availableActivities)
        {
            AppDetail app = new AppDetail();
            app.label = ri.loadLabel(manager);
            app.name = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(manager);
            hLayout.addView(createView(app,MM,sizeIcon,sizeAppView));

            if(helper.contains(app.name.toString())) {
                View tmp = createView(app,MM,sizeIcon,sizeAppViewOrd);
                layoutFavourite.addView(tmp);
                viewsFav.put(app.name.toString(),tmp);
            }

            if(layOrdered.get( (""+app.label.charAt(0)).toUpperCase() ) == null)
            {
                LinearLayout ll =  new LinearLayout(this);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                ll.setLayoutParams(MM);
                ll.setBackgroundResource(R.drawable.rounded);
                TextView label = new TextView(this);
                label.setText( (""+app.label.charAt(0)).toUpperCase() );
                label.setTextColor(Color.BLACK);
                label.setBackground(getResources().getDrawable(R.drawable.circle));
                label.setGravity(Gravity.CENTER);
                label.setTextSize(9);
                label.setLayoutParams(sizeIcon);
                ll.addView(label);
                layOrdered.put(("" + app.label.charAt(0)).toUpperCase(), ll);
                //Toast.makeText(this,"aaa",Toast.LENGTH_LONG).show();
            }
            layOrdered.get( (""+app.label.charAt(0)).toUpperCase() ).addView( createView(app,MM,sizeIcon,sizeAppViewOrd) );
        }

        LinearLayout vertLayout = new LinearLayout(this);
        vertLayout.setOrientation(LinearLayout.VERTICAL);
        vertLayout.setLayoutParams(MM);
        vertLayout.setGravity(Gravity.CENTER);

        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        AdView adv0 = (AdView)li.inflate(R.layout.adview,null);
        AdRequest adRequest0 = new AdRequest.Builder().build();
        adv0.loadAd(adRequest0);
        vertLayout.addView(adv0);

        HorizontalScrollView hsvf = new HorizontalScrollView(this);
        hsvf.addView(layoutFavourite);
        vertLayout.addView(hsvf);

        for(char c='A';c<='Z';c++)
            if(layOrdered.get(c+"")!=null) {
                HorizontalScrollView hsvo = new HorizontalScrollView(this);
                hsvo.setPadding(0,5,0,5);
                hsvo.setLayoutParams(MM);
                hsvo.addView(layOrdered.get(c+""));
                vertLayout.addView(hsvo);
            }

        AdView adv = (AdView)li.inflate(R.layout.adview2,null);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        adv.loadAd(adRequest1);
        vertLayout.addView(adv);

        sv.setBackground(getResources().getDrawable(R.drawable.rounded));
        sv.addView(vertLayout);

        leftContainer = new LinearLayout(this);
        leftContainer.setLayoutParams(MM);
        leftContainer.setOrientation(LinearLayout.VERTICAL);

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.addView(hLayout);
        hsv.setBackground(getResources().getDrawable(R.drawable.rounded));

        leftContainer.addView(hsv);
        leftContainer.addView(sv);

        slideDown(sv, true);

        container.addView(btnMove);
        container.addView(leftContainer);
    }

    private LinearLayout createView(final AppDetail app, LinearLayout.LayoutParams MW, LinearLayout.LayoutParams sizeIcon, LinearLayout.LayoutParams sizeAppView)
    {
        final LinearLayout appView = new LinearLayout(this);
        appView.setLayoutParams(sizeAppView);
        appView.setGravity(Gravity.CENTER);
        appView.setPadding(5,0,5,0);
        appView.setOrientation(LinearLayout.VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setLayoutParams(sizeIcon);
        icon.setImageDrawable(app.icon);

        ScrollView svApp = new ScrollView(this);
        LinearLayout layLabel = new LinearLayout(this);
        layLabel.setLayoutParams(MW);
        layLabel.setOrientation(LinearLayout.VERTICAL);
        layLabel.setGravity(Gravity.CENTER);
        TextView appLabel = new TextView(this);
        appLabel.setLayoutParams(MW);
        appLabel.setText(app.label);
        appLabel.setTextColor(Color.WHITE);
        appLabel.setGravity(Gravity.CENTER);
        appLabel.setTextSize(9);
        layLabel.addView(appLabel);
        svApp.addView(layLabel);
        appView.addView(icon);
        appView.addView(svApp);

        appView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showModal(app);
                return true;
            }
        });

        appView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                    appView.setAlpha(0.5f);
                else {appView.setAlpha(1f); }
                return false;
            }
        });

        appView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = manager.getLaunchIntentForPackage(app.name.toString());
                slideDown(leftContainer,false);
                btnMove.setAlpha(0.2f);
                isUpH = !isUpH;
                leftContainer.getContext().startActivity(i);
            }
        });

        return appView;
    }


    HashMap<String,View> viewsFav = new HashMap();
    private void showModal(final AppDetail apd)
    {
        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                400| ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                //WindowManager.LayoutParams.TYPE_INPUT_METHOD |
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW ,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER | Gravity.TOP;

        modal = li.inflate(R.layout.modal, null);

        Button yes = (Button) modal.findViewById(R.id.btn_yes);
        Button no = (Button) modal.findViewById(R.id.btn_no);

        TextView title = (TextView)modal.findViewById(R.id.title_diag);
        TextView message = (TextView)modal.findViewById(R.id.message_diag);

        title.setText(apd.label);
        message.setText(apd.name);

        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()== MotionEvent.ACTION_DOWN) v.setBackgroundColor(Color.rgb(100,100,255));
                else if(event.getAction()== MotionEvent.ACTION_UP) v.setBackgroundColor(Color.parseColor("#e9f3fa"));
                return false;
            }
        };
        yes.setOnTouchListener(onTouchListener);
        no.setOnTouchListener(onTouchListener);

        no.setText("Cancel");
        yes.setText("Info...");

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                windowManager.removeView(modal);
                modal = null;
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                final Intent i = new Intent();
                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.setData(Uri.parse("package:" + apd.name));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(i);
                windowManager.removeView(modal);
                modal = null;
                slideDown(leftContainer,false);
                isUpH = false;
            }
        });

        final CheckBox checkBox = (CheckBox)modal.findViewById(R.id.checkBoxFavourite);

        if(helper.contains(apd.name.toString())) {
            checkBox.setChecked(true);
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked){
                    View tmp = viewsFav.get(apd.name.toString());
                    if( tmp==null ) tmp=createView(apd,MM,sizeIcon,sizeAppViewOrd);
                    helper.insertData(apd.name.toString());
                    viewsFav.put(apd.name.toString(),tmp);
                    layoutFavourite.addView(tmp);
                }else{
                    helper.delete(apd.name.toString());
                    View tmp = viewsFav.remove(apd.name.toString());
                    layoutFavourite.removeView(tmp);
                }
            }
        });

        AdView mAdView1 = modal.findViewById(R.id.adView1);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        mAdView1.loadAd(adRequest1);

        windowManager.addView(modal, params);
    }



    public void slideUp(final View view, boolean slideY){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                slideY?0:view.getWidth(),                 // fromXDelta
                0,                 // toXDelta
                slideY?view.getHeight():0,  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }
    public void slideDown(final View view, boolean slideY){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                slideY?0:view.getWidth(),                 // toXDelta
                0,                 // fromYDelta
                slideY?view.getHeight():0); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        animate.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override public void onDestroy() {
        super.onDestroy();
        if (container != null) windowManager.removeView(container);
    }
    @Override public IBinder onBind(Intent intent) {
        return null;
    }

}
