package com.rsupport.jarinput;

import android.view.KeyEvent;
import android.view.MotionEvent;

import com.rsupport.util.conv;
import com.rsupport.util.rslog.MLog;

//typedef struct {
//	unsigned char	count;
//#	define RSUP_MONKEY_VER		0 // 2012.05.14
//	unsigned char	version;
//
//	union {
//		RU8 	action;
//      struct {
//          RU8 	action;
//          RU8		id; // currently alwasy 0.
//          short	x;
//          short	y;
//      } touch[1];
//      struct {
//          RU8 	action;
//          RU8		id; // currently alwasy 0.
//          short	x;
//          short	y;
//          short x2;
//          short y2;
//      } mtouch[1];
//      
//		struct {
//			RU8 	action;
//			char	delta; // -127 ~ +128
//			short	startX;
//			short	startY;
//		} wheel[1];
//
//		struct {
//			RU8		action;
//			RU16		keycode, keyscan, repeat;
//			RU32		meta;
//		} key[1];
//	};
//} rcpRsupMonkeyMsg;


public class MonkeyAgent implements IMonkeyHandler {
	static final int TouchActionDown = (0); // left mouse down.
	static final int TouchActionUp = (1); // left mouse up.
	static final int TouchActionMove = (2); // dragging. or trackball.
	// for multi touch.
	// static final int TouchActionDown = (5); // 2012.11.29
	// static final int TouchActionUp = (6); // 2012.11.29
	// static final int TouchActionMove = (7); // 2012.11.29

	static final int TouchActionScroll = 8; // scroll.

	static final int KeycodeActionDown = 0 + 100;
	static final int KeycodeActionUp = 1 + 100;

	static final int FlagSetting = 200;
	
	interface IInjector {
		public void touchInject(int action, int x, int y);

		public void wheelInject(int x, int y, int dy);

		public void keyInject(int action, int keycode, int scancode, int repeatCnt, int metaState);

		public void multiTouchEvent(int action, int x, int y, int x2, int y2);
	}

	IInjector mInjector;

	public MonkeyAgent() throws Exception {
		int osver = android.os.Build.VERSION.SDK_INT;

		if (0 <= osver && osver <= 15) // GB ~ ICS_MR1
			mInjector = new Injector23_40();
		else if (osver >= 16) // JB
			mInjector = new Injector41_42();
		else {
			mInjector = new InjectorNull();
			MLog.e("invalid android version: " + osver);
		}
	}

	// IAgent implementation.
	// keybd (int action, int code, int scancode, int repeatCnt, int metaState)
	// touch/wheel (int action, int delta, int x, int y, int x1, int y1)
	@Override
	public void handle(int action, int i1, int i2, int i3, int i4, int i5) {
		//if (log.DBG) log.d("action.%d, %d, %d, %d, %d, %d", action, i1, i2, i3, i4, i5);

		switch (action) {
		case TouchActionDown:
			// offset + 1 : skip id(1B), i2(2B), i3(2B)
			if (i5 == (short) 0x8000)
				mInjector.touchInject(MotionEvent.ACTION_DOWN, i2, i3);
			else
				mInjector.multiTouchEvent(MotionEvent.ACTION_DOWN, i2, i3, i4, i5);
			break;
		case TouchActionUp:
			if (i5 == (short) 0x8000)
				mInjector.touchInject(MotionEvent.ACTION_UP, i2, i3);
			else
				mInjector.multiTouchEvent(MotionEvent.ACTION_UP, i2, i3, i4, i5);
			break;
		case TouchActionMove:
			if (i5 == (short) 0x8000)
				mInjector.touchInject(MotionEvent.ACTION_MOVE, i2, i3);
			else
				mInjector.multiTouchEvent(MotionEvent.ACTION_MOVE, i2, i3, i4, i5);
			break;

		case TouchActionScroll:
			mInjector.wheelInject(i2, i3, i1); // x, y, dy
			break;

		case KeycodeActionDown:
			// keycode, scancode, repeat, meta
			mInjector.keyInject(KeyEvent.ACTION_DOWN, i1, i2, i3, i4); 
			break;
		case KeycodeActionUp:
			mInjector.keyInject(KeyEvent.ACTION_UP, i1, i2, i3, i4); 
			break;
			
		case FlagSetting:
			InjCommon.setFlags(i1,i2,i3,i4,i5);
			break;
			
		default:
			MLog.e("invalid action: " + action);
		}
	}

	@Override
	public void handle(byte[] bb, int offset, int len) {
		offset += 2; // skip count, version.
		try {
			assert (mInjector != null);
			for (; offset < len;) {
				int action = (0xff & bb[offset++]);
				switch (action) {
				case TouchActionDown:
					// offset + 1 : skip id(1B), x(2B), y(2B)
					if (len <= 7)
						mInjector.touchInject(MotionEvent.ACTION_DOWN, conv.byte2Toint(bb, offset + 1),
								conv.byte2Toint(bb, offset + 3));
					else
						mInjector.multiTouchEvent(MotionEvent.ACTION_DOWN, conv.byte2Toint(bb, offset + 1),
								conv.byte2Toint(bb, offset + 3), conv.byte2Toint(bb, offset + 5),
								conv.byte2Toint(bb, offset + 7));
					// offset += 5;
					break;
				case TouchActionUp:
					if (len <= 7)
						mInjector.touchInject(MotionEvent.ACTION_UP, conv.byte2Toint(bb, offset + 1),
								conv.byte2Toint(bb, offset + 3));
					else
						mInjector.multiTouchEvent(MotionEvent.ACTION_UP, conv.byte2Toint(bb, offset + 1),
								conv.byte2Toint(bb, offset + 3), conv.byte2Toint(bb, offset + 5),
								conv.byte2Toint(bb, offset + 7));
					// offset += 5;
					break;
				case TouchActionMove:
					if (len <= 7)
						mInjector.touchInject(MotionEvent.ACTION_MOVE, conv.byte2Toint(bb, offset + 1),
								conv.byte2Toint(bb, offset + 3));
					else
						mInjector.multiTouchEvent(MotionEvent.ACTION_MOVE, conv.byte2Toint(bb, offset + 1),
								conv.byte2Toint(bb, offset + 3), conv.byte2Toint(bb, offset + 5),
								conv.byte2Toint(bb, offset + 7));
					break;

				case TouchActionScroll:
					mInjector.wheelInject(conv.byte2Toint(bb, offset + 1), conv.byte2Toint(bb, offset + 3), bb[offset]);
					// offset += 5;
					break;

				case KeycodeActionDown:
					mInjector.keyInject(KeyEvent.ACTION_DOWN, conv.byte2Toint(bb, offset),
							conv.byte2Toint(bb, offset + 2), conv.byte2Toint(bb, offset + 4), // code,
																								// scan,
																								// repeat
							conv.byte4Toint(bb, offset + 6) // meta
							);
					// offset += 10;
					break;
				case KeycodeActionUp:
					mInjector.keyInject(KeyEvent.ACTION_UP, conv.byte2Toint(bb, offset),
							conv.byte2Toint(bb, offset + 2), conv.byte2Toint(bb, offset + 4), // code,
																								// scan,
																								// repeat
							conv.byte4Toint(bb, offset + 6) // meta
							);
					// offset += 10;
					break;
				case FlagSetting:
					InjCommon.setFlags(
							conv.byte2Toint(bb, offset), 
							conv.byte2Toint(bb, offset+2),
							conv.byte2Toint(bb, offset+4),
							conv.byte2Toint(bb, offset+6),
							conv.byte2Toint(bb, offset+8)
							);
					break;
				default:
					MLog.e("invalid action: " + action);
				}
			}
		} catch (Exception e) {
			MLog.w(e.toString());
		}
	}

}
