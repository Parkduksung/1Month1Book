package com.rsupport.srn30.screen;

import android.content.Context;

import com.rsupport.rsperm.AbstractPermission;
import com.rsupport.rsperm.IEnginePermission;
import com.rsupport.rsperm.KnoxPermission;
import com.rsupport.rsperm.ProjectionPermission;
import com.rsupport.rsperm.RSPermission;
import com.rsupport.rsperm.RSPermissionForProjection;
import com.rsupport.rsperm.SonyPermission;
import com.rsupport.rsperm.UDSPermission;
import com.rsupport.util.LauncherUtils;
import com.rsupport.util.PermissionUtils;
import com.rsupport.util.rslog.MLog;

public class PermissionLoader {

	/**
	 * Permission 우선순위 UDS
	 */
	public static final int PRIORITY_UDSPERM =		0x00000001;

	/**
	 * Permission 우선순위 Rsperm
	 */
	public static final int PRIORITY_RSPERM =		0x00000002;

    /**
     * Permission 우선순 Projection
     */
    public static final int PRIORITY_PROJECTION =		0x00000004;

    /**
     * Permission 우선순위 Sony
     */
    public static final int PRIORITY_SONY =		0x00000008;

    /**
     * Permission 우선순위 Knox
     */
    public static final int PRIORITY_KNOX_PROJECTION = 0x00000010;

    /**
     * Permission 권한 유지 (현재는 PROJECTION 만 사용
     */
    public static final int FLAG_MAINTAIN_PERM =	0x01000000;

	/**
	 * Permission 우선순위 퍼미션만 
	 */
	public static final int FLAG_PRIORITY_PERM_ONLY =	0x10000000;
		
	public static IEnginePermission createEnginePermission(Context context, String packageName, int permissionPriority){
        boolean isPermOnly = (permissionPriority&FLAG_PRIORITY_PERM_ONLY) == FLAG_PRIORITY_PERM_ONLY;
		if((permissionPriority&PRIORITY_UDSPERM) != 0) {
            return createPriorityUDS(context, packageName, isPermOnly);
        }
        else if((permissionPriority&PRIORITY_RSPERM) != 0) {
            return createPriorityRsperm(context, packageName, isPermOnly);
        }
        else if((permissionPriority&PRIORITY_SONY) != 0) {
            return createPrioritySony(context, packageName, isPermOnly);
        }
        else if((permissionPriority&PRIORITY_PROJECTION) != 0) {
            boolean isMaintain = (permissionPriority&FLAG_MAINTAIN_PERM) == FLAG_MAINTAIN_PERM;
            return createPriorityProjection(context, packageName, isPermOnly, isMaintain);
        }
        else if((permissionPriority&PRIORITY_KNOX_PROJECTION) != 0) {
            boolean isMaintain = (permissionPriority&FLAG_MAINTAIN_PERM) == FLAG_MAINTAIN_PERM;
            return createPriorityKnoxProjection(context, packageName, isPermOnly, isMaintain);
        }
        MLog.e("not support mode : " + permissionPriority);
        return null;
	}

    public static void release(IEnginePermission permission) {
        if(permission instanceof AbstractPermission){
            ((AbstractPermission)permission).onDestroy();
        }
    }
	
	private static IEnginePermission createPriorityRsperm(Context context, String packageName,
                                                          boolean isPermOnly){
        MLog.i("createPriorityRsperm");
		IEnginePermission permission = createRSPermission(context, packageName);
		if(permission != null){
			MLog.i("RSPermission bound");
			return permission;
		}

        if(isPermOnly == true){
            MLog.i("createPriorityRsperm isPermOnly");
            return null;
        }
		
		permission = createUDSPermission(context);
		if(permission != null){
			MLog.i("UDSPermission bound");
			return permission;
		}
		
		permission = createSonyPermission(context);
		if(permission != null){
			MLog.i("SonyPermission bound");
			return permission;
		}
		
		permission = createProjectionPermission(context, false);
		if(permission != null){
			MLog.i("ProjectionPermission bound");
			return permission;
		}
		return permission;
	}
	
	private static IEnginePermission createPriorityUDS(Context context, String packageName,
                                                       boolean isPermOnly){
		IEnginePermission permission = createUDSPermission(context);
		if(permission != null){
			MLog.i("UDSPermission bound");
			return permission;
		}

        if(isPermOnly == true){
            MLog.i("createPriorityUDS isPermOnly");
            return null;
        }
		
		permission = createRSPermission(context, packageName);
		if(permission != null){
			MLog.i("RSPermission bound");
			return permission;
		}
		
		permission = createSonyPermission(context);
		if(permission != null){
			MLog.i("SonyPermission bound");
			return permission;
		}
		
		permission = createProjectionPermission(context, false);
		if(permission != null){
			MLog.i("ProjectionPermission bound");
			return permission;
		}
		
		return permission;
	}

    private static IEnginePermission createPrioritySony(Context context, String packageName,
                                                          boolean isPermOnly){
        IEnginePermission permission = createSonyPermission(context);
        if(permission != null){
            MLog.i("SonyPermission bound");
            return permission;
        }

        if(isPermOnly == true){
            MLog.i("createPrioritySony isPermOnly");
            return null;
        }

        permission = createUDSPermission(context);
        if(permission != null){
            MLog.i("UDSPermission bound");
            return permission;
        }

        permission = createRSPermission(context, packageName);
        if(permission != null){
            MLog.i("RSPermission bound");
            return permission;
        }

        permission = createProjectionPermission(context, false);
        if(permission != null){
            MLog.i("ProjectionPermission bound");
            return permission;
        }
        return permission;
    }

    private static IEnginePermission createPriorityProjection(Context context, String packageName,
                                                        boolean isPermOnly, boolean isMaintainPermission){

        IEnginePermission permission = createProjectionPermission(context, isMaintainPermission);
        if(permission != null){
            MLog.i("ProjectionPermission bound");
            return permission;
        }

        if(isPermOnly == true){
            MLog.i("createPriorityProjection isPermOnly");
            return null;
        }

        permission = createUDSPermission(context);
        if(permission != null){
            MLog.i("UDSPermission bound");
            return permission;
        }

        permission = createRSPermission(context, packageName);
        if(permission != null){
            MLog.i("RSPermission bound");
            return permission;
        }

        permission = createSonyPermission(context);
        if(permission != null){
            MLog.i("SonyPermission bound");
            return permission;
        }
        return permission;
    }

    private static IEnginePermission createPriorityKnoxProjection(Context context, String key,
                                                              boolean isPermOnly, boolean isMaintainPermission){

        IEnginePermission permission = createKnoxProjectionPermission(context, key, isMaintainPermission);
        if(permission != null){
            MLog.i("KnoxProjectionPermission bound");
            return permission;
        }

        if(isPermOnly == true){
            MLog.i("createPriorityProjection isPermOnly");
            return null;
        }

        permission = createUDSPermission(context);
        if(permission != null){
            MLog.i("UDSPermission bound");
            return permission;
        }

        permission = createRSPermission(context, key);
        if(permission != null){
            MLog.i("RSPermission bound");
            return permission;
        }

        permission = createSonyPermission(context);
        if(permission != null){
            MLog.i("SonyPermission bound");
            return permission;
        }
        return permission;
    }

    private static IEnginePermission createKnoxProjectionPermission(Context context, String key, boolean useMaintainPermission){

        if(PermissionUtils.isAvailableMediaProjection(context) == false){
            return null;
        }
        KnoxPermission permission = new KnoxPermission();

        permission.setContext(context);
        permission.setUseMaintainPermission(useMaintainPermission);
        if(permission.bind(key) == true){
            return permission;
        }
        permission.onDestroy();
        permission = null;
        return permission;
    }

    private static IEnginePermission createProjectionPermission(Context context, boolean useMaintainPermission){

        if(PermissionUtils.isAvailableMediaProjection(context) == false){
            return null;
        }
        ProjectionPermission permission = new ProjectionPermission();

        permission.setContext(context);
        permission.setUseMaintainPermission(useMaintainPermission);
        if(permission.bind(null) == true){
            return permission;
        }
        permission.onDestroy();
        permission = null;
        return permission;
    }

	private static IEnginePermission createUDSPermission(Context context){
        if(LauncherUtils.isAliveLauncher(context) == false){
            if(LauncherUtils.executeLauncher(context, true) == false){
                MLog.i("executeLauncher fail");
            }
        }

		AbstractPermission permission = new UDSPermission();
		permission.setContext(context);
		if(permission.bind(null) == true){
			return permission;
		}
		permission.onDestroy();
		permission = null;
		return null;
	}

	private static IEnginePermission createRSPermission(Context context, String packageName) {
        MLog.i("createRSPermission : " + packageName);
		if(packageName == null || "".equals(packageName) == true){
			return null;
		}

        AbstractPermission permission = null;

        if(PermissionUtils.isSupportReadFrameBuffer(context, packageName) == true ||
                PermissionUtils.isSupportVirtualDisplay(context, packageName)){

            permission = new RSPermission();
            permission.setContext(context);
            if(permission.bind(packageName) == true){
                int[] supportEncoder = permission.getSupportEncoder();
                if(supportEncoder != null && supportEncoder.length > 0){
                    return permission;
                }
            }
            permission.onDestroy();
        }

        if(PermissionUtils.isAvailableMediaProjection(context) == true &&
                PermissionUtils.isSupportInject(context, packageName)){
            permission = new RSPermissionForProjection();
            permission.setContext(context);
            if(permission.bind(packageName) == true){
                int[] supportEncoder = permission.getSupportEncoder();
                if(supportEncoder != null && supportEncoder.length > 0){
                    return permission;
                }
            }
            permission.onDestroy();
            permission = null;
        }
		return null;
	}
	
	private static IEnginePermission createSonyPermission(Context context) {
		AbstractPermission permission = new SonyPermission();
		permission.setContext(context);
		if(permission.bind(null) == true){
			return permission;
		}
		permission.onDestroy();
		permission = null;
		return null;
	}
}
