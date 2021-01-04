package com.rsupport.srn30.screen.channel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;

import com.rsupport.srn30.screen.encoder.ScapOption;
import com.rsupport.util.Screen;
import com.rsupport.util.rslog.MLog;

public class Srn30Packet {

	static int _iRotate = -1;
	
	static public Rect screenRect;
	
	public static boolean init(Context cxt, int hwRotation) {
		Point size = Screen.resolution(cxt);
		if ((hwRotation %2) == 0)
			screenRect = new Rect(0,0,size.x,size.y);
		else
			screenRect = new Rect(0,0,size.y,size.x);
		return true;
	}

	public static ByteBuffer scapNotifyMsg(int code, String errMsg) {
		ByteBuffer bb = ByteBuffer.allocate(256).order(ByteOrder.LITTLE_ENDIAN);
		bb.put((byte) scap.scapNotify); // type
		bb.put((byte) code); // code
		bb.putShort((short) errMsg.length()); // length.
		bb.put(errMsg.getBytes());
		return bb;
	}

	public static ByteBuffer scapOption2Msg(int rotation, ScapOption scapOption) {
		ByteBuffer bb = ByteBuffer.allocate(256).order(ByteOrder.LITTLE_ENDIAN);

		bb.put((byte) scap.scapOption2); // type
		bb.put((byte) 0); // subtype
		bb.putShort((short) 0x0fff); // flags

		if ("scapOptDeskMsg" != null) {
			bb.put((byte) 'P'); // scapHookType;
			bb.put((byte) 0); // scapHookMonitor; ///< monitor 0, 1, 2, ... 0 :
								// all monitor, 1 : primary, 2 : secondary, 3,
								// 4, 5,
			bb.put((byte) 32); // scapLocalPxlCnt; ///< host pixel depth

			_iRotate = rotation;
			bb.put((byte) rotation); // scapRotate; - not used.
			bb.putInt(0); // scapHookFlags; ///< hooking flags

			bb.putShort((short) 0); // scapTriggingTime; unit send timeout (def 40ms)
			bb.putShort((short) 0); // scapPad2;

			bb.putShort((short) 0); // xRatio.num : moved to encoder info.
			bb.putShort((short) 0); // xRatio.denum
			bb.putShort((short) 0); // yRatio.num : moved to encoder info.
			bb.putShort((short) 0); // tRatio.denum

			bb.putInt(0); // encBitrate; ///< server side stretch (encBitrate/real_width)
			bb.putInt(0); // encFPS;///< server side stretch (encFPS/real_height)

			bb.putInt(screenRect.left); // rcSrn.left, host ROI screen coordinate.
			bb.putInt(screenRect.top); // rcSrn.top, host ROI screen coordinate.
			bb.putInt(screenRect.right); // rcSrn.right, host ROI screen coordinate.
			bb.putInt(screenRect.bottom); // rcSrn.bottom, host ROI screen coordinate.
		}

		if ("scapOptEncMsg" != null) {
			bb.putInt(0); // flags;
			bb.put((byte) scapOption.getEncoderType()); // scapEncoderType;

			bb.put((byte) 0x0f); // ValidFlags; ///< not used
			bb.put((byte) 32); // HostPxlCnt; ///< desktop pixel count(Host -> Viewer).
			bb.put((byte) 32); // scapRemoteBpp; ///< encoder pixel count(1, 4, 8, 15/16, 24, 32 : Viewer <-> Host)

			bb.put((byte) 0); // scapEncJpgLowQuality; ///< for high freq changed region (0~50%) : default 30%
			bb.put((byte) scapOption.getJpegQuality()); // scapEncJpgHighQuality; for low freq changed region (50~80%) : default 75%
			bb.put((byte) 0); // scapRemoteBpp3G; ///< only mobile(default 3g-color)
			bb.put((byte) 0); // scapRemoteBppWifi; ///< only mobile(default wifi-color)

			bb.putInt(0); // scapTileCacheCount; ///< Graph caching size : 64x64 unit count (def 2048)

			short numerator = (short) scapOption.getStretch().x;
			short denominator = (short) scapOption.getStretch().y;
			bb.putShort(numerator); // xRatio
			bb.putShort(denominator);
			bb.putShort(numerator); // yRatio
			bb.putShort(denominator);

			bb.putInt(scapOption.getBitrate()); // encBitrate; ///< for omx, hwenc, vpx
			bb.putInt(0); // encFPS;
		}

		return bb;
	}

	public static ByteBuffer scapRotationMsg(int rotateIdx, boolean isDrm, ScapOption scapOption) {
		if (_iRotate == rotateIdx)
			return null;

		MLog.i("rotation: %d -> %d", _iRotate, rotateIdx);
		_iRotate = rotateIdx;

		// scapRotationMsg
		ByteBuffer bb = ByteBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN);
		bb.put((byte) scap.scapRotation); // type
		bb.put((byte) rotateIdx); // rotationIndex
		bb.put((byte) (isDrm ? 1 : 0)); // isDRM
		bb.put((byte) 0); // padding

		bb.putInt(screenRect.left); // rcSrn.left, host ROI screen coordinate.
		bb.putInt(screenRect.top); // rcSrn.top, host ROI screen coordinate.
		bb.putInt(screenRect.right); // rcSrn.right, host ROI screen coordinate.
		bb.putInt(screenRect.bottom); // rcSrn.bottom, host ROI screen coordinate.

		bb.putInt(scapOption.getStretch().x); // ratioImage
		bb.putInt(scapOption.getStretch().y);
		// TileCache::setPrefixCrc32(rotateIdx * 90);
		return bb;
	}

	public static ByteBuffer scapVersionMsg(int hwRotation) {
		int sdkVer = Build.VERSION.SDK_INT;
		final int FEATURE_MONKEY = (0x0001);

		// scapVersionMsg
		ByteBuffer bb = ByteBuffer.allocate(128).order(ByteOrder.LITTLE_ENDIAN);
		bb.put((byte) scap.scapVersion); // type
		bb.put((byte) hwRotation);
		bb.putShort((short) (FEATURE_MONKEY & 0xffff)); // features;
		bb.putInt(sdkVer); // 1~19 (1.0 ~ 4.4) 
		bb.putInt(0); // version; //
		bb.putShort((short) 0); // cpuFamily; // [2013.03.21 for omx]
		bb.putShort((short) screenRect.left); // rcSrn.
		bb.putShort((short) screenRect.top);
		bb.putShort((short) screenRect.right);
		bb.putShort((short) screenRect.bottom);
		bb.position(bb.position() + 20 - 10);
		return bb;
	}
}

// get unsigned short : int value = buf.getShort() & 0xffff;