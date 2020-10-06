package com.rsupport.mobile.agent.ui.tutorial;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.repo.config.ConfigRepository;

import org.koin.java.KoinJavaComponent;

import kotlin.Lazy;

import com.rsupport.mobile.agent.ui.base.RVCommonActivity;


public class TutorialActivity extends RVCommonActivity implements OnItemSelectedListener {

    private TutorialGallery gallery;
    private ImageView[] indexImageView;
    private BaseAdapter tutorialAdapter;
    private int position;
    private boolean isAboutPage;
    private short tutorialType;
    private Button btnStart, btnLandStart;

    private boolean isAgentCall;

    private Lazy<ConfigRepository> configRepositoryLazy = KoinJavaComponent.inject(ConfigRepository.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        isAgentCall = intent.hasExtra("AGENT_CALL");

        Bundle order = getIntent().getExtras();
        if (order != null) {
            isAboutPage = order.getBoolean("about", false);
            tutorialType = order.getShort(TutorialSelectActivity.TUTORIAL_TYPE, TutorialSelectActivity.TUTORIAL_TYPE_PERSON);
        }

        configRepositoryLazy.getValue().setShowTutorial(true);

        setContentView(R.layout.tutorial, R.layout.layout_common_bg_ns_no_margin);
        setTitle(R.string.tutorial_title, true, false);

        setLeftButtonBackground(R.drawable.button_headerback);

        ImageButton btnTitleLeft = (ImageButton) findViewById(R.id.left_button);
        btnTitleLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startRightSlideAnimation();

                if (isAboutPage) {
                    finish();
                } else {
                    backActivity(TutorialActivity.this);
                }
            }
        });
        btnTitleLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isAgentCall)
                    v.requestFocusFromTouch();
                return false;
            }
        });

        int[] indexArray = {R.id.index_0, R.id.index_1, R.id.index_2/*, R.id.index_3, R.id.index_4*/};

        indexImageView = new ImageView[indexArray.length];
        for (int i = 0; i < indexArray.length; i++) {
            indexImageView[i] = (ImageView) findViewById(indexArray[i]);
        }

        gallery = (TutorialGallery) findViewById(R.id.introgallery);
        gallery.setScrollType(TutorialGallery.TYPE_MOTION);

        tutorialAdapter = new ImageAdapter(this);
        gallery.setAdapter(tutorialAdapter);
        gallery.setOnItemSelectedListener(this);
        gallery.setCallbackDuringFling(false);

        btnStart = (Button) findViewById(R.id.rvstart);
        btnStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startRemoteView();
            }
        });
        btnStart.setVisibility(View.INVISIBLE);
    }

    void startRemoteView() {
        startRightSlideAnimation();
        finish();
    }

    private void backActivity(Context context) {
//		Editor editor = getSharedPreferences(PREFERENCES_TUTORIAL, Context.MODE_PRIVATE).edit();
//		editor.putBoolean(PREFERENCES_TUTORIAL_OCCURED, false);
//		editor.commit();

//		Intent intent = new Intent(context, rsupport.AndroidViewer.tutorial.TutorialSelectActivity.class);
//		intent.putExtra(PreferenceConstant.RV_BUNDLE_ACTIVITY_BACK, PreferenceConstant.RV_TRUE);
//		startActivity(intent);
        finish();
    }

    private void movePrev(View view) {
        if (view != null) {

            int originScrollType = gallery.getScrollType();

            int eventWidth = view.getWidth() / 2 + 50;
            if (gallery.getSelectedItemPosition() == gallery.getCount() - 1) {
                eventWidth = view.getWidth() * (gallery.getCount() - 1);
            }
            int width = eventWidth;
            MotionEvent e1 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0);
            gallery.setScrollType(TutorialGallery.TYPE_MOTION);
            gallery.dispatchTouchEvent(e1);
            for (int i = 0; i < width; i += 20) {
                MotionEvent e2 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, i, 0, 0);
                gallery.dispatchTouchEvent(e2);
            }
            MotionEvent e3 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, width, 0, 0);
            gallery.dispatchTouchEvent(e3);

            if (originScrollType == TutorialGallery.TYPE_KEY) {
                gallery.setScrollType(TutorialGallery.TYPE_KEY);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (isAboutPage) {
                finish();
            } else {
                backActivity(TutorialActivity.this);
            }
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
        }
        return super.onKeyDown(keyCode, event);
    }

    private void moveNext(View view) {
        if (view != null) {

            int originScrollType = gallery.getScrollType();

            int width = view.getWidth() / 2 + 50;
            MotionEvent e1 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, width, 0, 0);
            gallery.setScrollType(TutorialGallery.TYPE_MOTION);
            gallery.dispatchTouchEvent(e1);
            for (int i = width; i > 0; i -= 20) {
                MotionEvent e2 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, i, 0, 0);
                gallery.dispatchTouchEvent(e2);
            }
            MotionEvent e3 = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0);
            gallery.dispatchTouchEvent(e3);

            if (originScrollType == TutorialGallery.TYPE_KEY) {
                gallery.setScrollType(TutorialGallery.TYPE_KEY);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        this.position = position;
        setPositionDot(position);

        if (position < indexImageView.length - 1 || isAboutPage) {
            if (btnStart != null) btnStart.setVisibility(View.INVISIBLE);
        } else {
            if (btnStart != null) btnStart.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }


    private void setPositionDot(int index) {
        if (indexImageView != null) {
            for (int i = 0; i < indexImageView.length; i++) {
                indexImageView[i].setEnabled((index == i) ? true : false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gallery = null;
        if (indexImageView != null) {
            for (int i = 0; i < indexImageView.length; i++) {
                indexImageView[i] = null;
            }
        }
        indexImageView = null;
        tutorialAdapter = null;
    }

    class ImageAdapter extends BaseAdapter {

        private Integer[] imageIds;

        private Integer[] personImageIds = {
                R.drawable.img_tutorial01,
                R.drawable.img_tutorial02,
                R.drawable.img_tutorial03,
//				R.drawable.img_tutorial04,
//				R.drawable.img_tutorial05,
        };

        private Integer[] corpImageIds = {
                R.drawable.img_tutorial01,
                R.drawable.img_tutorial02,
                R.drawable.img_tutorial03,
//				R.drawable.img_tutorial04,
//				R.drawable.img_tutorial05,
        };

//		private Integer[] chinaPersonImageIds = {
//				R.drawable.tutorial1,
//				R.drawable.tutorial2_cn,
//				R.drawable.tutorial3_cn,
//				R.drawable.tutorial4,
//				R.drawable.tutorial5,
//		};
//		
//		private Integer[] chinaCorpImageIds = {
//				R.drawable.tutorial1,
//				R.drawable.tutorial2_corp_cn,
//				R.drawable.tutorial3_corp_cn,
//				R.drawable.tutorial4_corp_cn,
//				R.drawable.tutorial5,
//		};

        public ImageAdapter(Context c) {
            if (tutorialType == TutorialSelectActivity.TUTORIAL_TYPE_PERSON) {
                imageIds = personImageIds;
            } else if (tutorialType == TutorialSelectActivity.TUTORIAL_TYPE_CORP) {
                imageIds = corpImageIds;
            } else if (tutorialType == TutorialSelectActivity.TUTORIAL_TYPE_CHINA_PERSON) {
                imageIds = personImageIds; // chinaPersonImageIds;
            } else if (tutorialType == TutorialSelectActivity.TUTORIAL_TYPE_CHINA_CORP) {
                imageIds = corpImageIds;   // chinaCorpImageIds;
            }
        }

        public int getCount() {
            return imageIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        private class Holder {
            public RelativeLayout rootLayout;
            public TextView summaryView;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout rootLayout = null;
            Holder holder = null;

            if (convertView == null) {
                rootLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.tutorial_gallery, parent, false);

                ((ImageView) rootLayout.findViewById(R.id.tutorial_image)).setImageResource(imageIds[position]);
                TextView summaryView = ((TextView) rootLayout.findViewById(R.id.tutorial_summary));

                holder = new Holder();
                holder.rootLayout = rootLayout;
                holder.summaryView = summaryView;

                rootLayout.setTag(holder);

            } else {
                rootLayout = (RelativeLayout) convertView;
                holder = (Holder) rootLayout.getTag();
            }

            if (tutorialType == TutorialSelectActivity.TUTORIAL_TYPE_PERSON) {
                visiblePersonView(holder.summaryView, rootLayout, position);
            } else if (tutorialType == TutorialSelectActivity.TUTORIAL_TYPE_CORP) {
                visibleCorpView(holder.summaryView, rootLayout, position);
            } else if (tutorialType == TutorialSelectActivity.TUTORIAL_TYPE_CHINA_PERSON) {
                visibleChinaPersonView(holder.summaryView, rootLayout, position);
            } else if (tutorialType == TutorialSelectActivity.TUTORIAL_TYPE_CHINA_CORP) {
                visibleChinaCorpView(holder.summaryView, rootLayout, position);
            }

            return rootLayout;
        }

        private void visiblePersonView(TextView summaryView, View rootLayout, int position) {
            switch (position) {
                case 0:
                    summaryView.setText(R.string.tutorial_summary_1);
                    rootLayout.findViewById(R.id.tutorial_desc_1).setVisibility(View.VISIBLE);
                    break;
                case 1:
                    summaryView.setText(R.string.tutorial_summary_2);
                    rootLayout.findViewById(R.id.tutorial_desc_2_1).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_desc_2_2).setVisibility(View.VISIBLE);
//				rootLayout.findViewById(R.id.tutorial_desc_2_3).setVisibility(View.VISIBLE);
                    break;
                case 2:
                    summaryView.setText(R.string.tutorial_summary_3);
                    rootLayout.findViewById(R.id.tutorial_desc_3_1).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_desc_3_2).setVisibility(View.VISIBLE);
//				rootLayout.findViewById(R.id.tutorial_desc_3_3).setVisibility(View.VISIBLE);
                    break;
//			case 3:
//				summaryView.setText(R.string.tutorial_summary_4);
//				rootLayout.findViewById(R.id.tutorial_desc_4_1).setVisibility(View.VISIBLE);
//				rootLayout.findViewById(R.id.tutorial_desc_4_2).setVisibility(View.VISIBLE);
//				rootLayout.findViewById(R.id.tutorial_desc_4_3).setVisibility(View.VISIBLE);
//				rootLayout.findViewById(R.id.tutorial_desc_4_4).setVisibility(View.VISIBLE);
//				break;
//			case 4:
//				summaryView.setText(R.string.tutorial_summary_5);
//				rootLayout.findViewById(R.id.tutorial_desc_5_1).setVisibility(View.VISIBLE);
//				rootLayout.findViewById(R.id.tutorial_desc_5_2).setVisibility(View.VISIBLE);
//				rootLayout.findViewById(R.id.tutorial_desc_5_3).setVisibility(View.VISIBLE);
//				break;
            }
        }

        private void visibleCorpView(TextView summaryView, View rootLayout, int position) {
            switch (position) {
                case 0:
                    summaryView.setText(R.string.tutorial_summary_1);
                    rootLayout.findViewById(R.id.tutorial_desc_1).setVisibility(View.VISIBLE);
                    break;
                case 1:
                    summaryView.setText(R.string.tutorial_summary_2);
                    rootLayout.findViewById(R.id.tutorial_desc_2_1).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_desc_2_2).setVisibility(View.VISIBLE);
//				rootLayout.findViewById(R.id.tutorial_desc_2_3).setVisibility(View.VISIBLE);
                    break;
                case 2:
                    summaryView.setText(R.string.tutorial_summary_3);
                    rootLayout.findViewById(R.id.tutorial_desc_3_1).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_desc_3_2).setVisibility(View.VISIBLE);
//				rootLayout.findViewById(R.id.tutorial_desc_3_3).setVisibility(View.VISIBLE);
                    break;
//			case 3:
//				summaryView.setText(R.string.tutorial_summary_4);
//				rootLayout.findViewById(R.id.tutorial_desc_4_1).setVisibility(View.VISIBLE);
//				rootLayout.findViewById(R.id.tutorial_desc_4_2).setVisibility(View.VISIBLE);
//				rootLayout.findViewById(R.id.tutorial_desc_4_3).setVisibility(View.VISIBLE);
//				rootLayout.findViewById(R.id.tutorial_desc_4_4).setVisibility(View.VISIBLE);
//				break;
//			case 4:
//				summaryView.setText(R.string.tutorial_summary_5);
//				rootLayout.findViewById(R.id.tutorial_desc_5_1).setVisibility(View.VISIBLE);
//				rootLayout.findViewById(R.id.tutorial_desc_5_2).setVisibility(View.VISIBLE);
//				rootLayout.findViewById(R.id.tutorial_desc_5_3).setVisibility(View.VISIBLE);
//				break;
            }
        }

        private void visibleChinaPersonView(TextView summaryView, View rootLayout, int position) {
            switch (position) {
                case 0:
                    summaryView.setText(R.string.tutorial_summary_1);
                    rootLayout.findViewById(R.id.tutorial_desc_1).setVisibility(View.VISIBLE);
                    break;
                case 1:
                    summaryView.setText(R.string.tutorial_summary_2);
                    rootLayout.findViewById(R.id.tutorial_desc_2_1).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_cn_desc_2_2).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_desc_2_3).setVisibility(View.VISIBLE);
                    break;
                case 2:
                    summaryView.setText(R.string.tutorial_summary_3);
                    rootLayout.findViewById(R.id.tutorial_desc_3_1).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_cn_desc_3_2).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_desc_3_3).setVisibility(View.VISIBLE);
                    break;
                case 3:
                    summaryView.setText(R.string.tutorial_summary_4);
                    rootLayout.findViewById(R.id.tutorial_desc_4_1).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_desc_4_2).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_desc_4_3).setVisibility(View.VISIBLE);
                    break;
                case 4:
                    summaryView.setText(R.string.tutorial_summary_5);
                    rootLayout.findViewById(R.id.tutorial_desc_5_1).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_desc_5_2).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_desc_5_3).setVisibility(View.VISIBLE);
                    break;
            }
        }

        private void visibleChinaCorpView(TextView summaryView, View rootLayout, int position) {
            switch (position) {
                case 0:
                    summaryView.setText(R.string.tutorial_summary_1);
                    rootLayout.findViewById(R.id.tutorial_desc_1).setVisibility(View.VISIBLE);
                    break;
                case 1:
                    summaryView.setText(R.string.tutorial_summary_2);
                    rootLayout.findViewById(R.id.tutorial_corp_desc_2_1).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_cn_corp_desc_2_2).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_corp_desc_2_3).setVisibility(View.VISIBLE);
                    break;
                case 2:
                    summaryView.setText(R.string.tutorial_summary_3);
                    rootLayout.findViewById(R.id.tutorial_desc_3_1).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_cn_corp_desc_3_2).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_corp_desc_3_3).setVisibility(View.VISIBLE);
                    break;
                case 3:
                    summaryView.setText(R.string.tutorial_summary_4);
                    rootLayout.findViewById(R.id.tutorial_desc_4_1).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_desc_4_2).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_desc_4_3).setVisibility(View.VISIBLE);
                    break;
                case 4:
                    summaryView.setText(R.string.tutorial_summary_5);
                    rootLayout.findViewById(R.id.tutorial_desc_5_1).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_desc_5_2).setVisibility(View.VISIBLE);
                    rootLayout.findViewById(R.id.tutorial_desc_5_3).setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
