//
// FOR TESTING
//
package com.rsupport.rsperm;

/*

//
public class RsPermAct extends Activity implements OnClickListener {
	
	
	Context mContext;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
		log.i("onCreate done");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        mContext = getApplicationContext();
    	getButton(R.id.button1).setOnClickListener(this);
		getButton(R.id.button2).setOnClickListener(this);
		getButton(R.id.button3).setOnClickListener(this);
		getButton(R.id.button4).setOnClickListener(this);
		getButton(R.id.button5).setOnClickListener(this);


		getButton(R.id.button1).setText("start MediaProjection");
		getButton(R.id.button2).setText("start VirtualDisplay");
    }
    Button getButton(int id) { return (Button)findViewById(id); }
    
    void startMediaProjectionActivity() {
        Intent intent = new Intent(this, MPTriggerActivity.class);
        startActivity(intent);
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
            startMediaProjectionActivity();
			break;
		case R.id.button2:
			createVirtualDisplay();
			break;
		case R.id.button3:
			break;
		case R.id.button4: // load remote library.
			break;
		case R.id.button5: // close.
			break;
		}
	}

	//
	//
	//

	int mWidth = 1080;
	int mHeight = 1920;
	VirtualDisplay mVD;
	@TargetApi(19)
	void createVirtualDisplay() {
		WindowManager wm = (WindowManager)this.getSystemService(Context.WINDOW_SERVICE);
		DisplayManager dm = (DisplayManager)this.getSystemService(Context.DISPLAY_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(metrics);

		int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR;
		mVD = dm.createVirtualDisplay("VDTest", mWidth, mHeight, metrics.densityDpi,
				getSurface(mWidth, mHeight), flags);

		if (mVD != null)
		log.i("after virtual display (%dx%d)", mWidth, mHeight);
		//mVD.resize(size.getWidth(), size.getHeight(), metrics.densityDpi);
		//mVD.setSurface(surface);
	}


	private ImageReader mImageReader;
	private Handler mHandler;
	@TargetApi(21)
	Surface getSurface(int width, int height)
	{
		if (mImageReader != null) {
			if (mImageReader.getWidth() != width || mImageReader.getHeight() != height) {
				Image i = mImageReader.acquireLatestImage();
				if (i != null) i.close();
				mImageReader.setOnImageAvailableListener(null, null);
				mImageReader.close();
				mImageReader = null;
			}
		}

		if (mImageReader == null) {
			mHandler = new Handler(RsupApplication.context.getMainLooper());
			mImageReader = ImageReader.newInstance( width, height, PixelFormat.RGBA_8888, 2);
			mImageReader.setOnImageAvailableListener( new NewImageAvailableListener(), mHandler);
		}

		Surface surf = mImageReader.getSurface();
		if (surf != null) log.i("Surfacce created.");
		return surf;
	}


	private final Lock mIRLock = new ReentrantLock(true); // fair
	@TargetApi(21)
	class NewImageAvailableListener implements
			ImageReader.OnImageAvailableListener {
		@Override
		public void onImageAvailable(ImageReader reader) {

			mIRLock.lock();
			try {
				Image image = reader.acquireLatestImage();
				if (image != null) {
					Image.Plane[] planes = image.getPlanes();
					Image.Plane plane = planes[0];
					int stride = plane.getRowStride();
					int width = image.getWidth();
					int height = image.getHeight();
					int format = image.getFormat();
					//long tick  = image.getTimestamp();
					//Rect rcCrop = image.getCropRect();

					ByteBuffer bb = plane.getBuffer();
					if (log.DBG)
						log.w("new image: stride=%d, capacitiy=%d, position=%d, limit=%d",
								stride, bb.capacity(), bb.position(), bb.limit());
					//i.native_update(bb, width, height, format, stride);

					image.close();
				}
			} catch (Exception e) {
			}
			mIRLock.unlock();
		}
	}


}
//*/