package net.accessiblility.app.store.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import net.accessiblility.app.store.model.AppInfo;
import net.accessiblility.app.store.model.DownloadInfo;
import net.accessiblility.app.store.model.LocalAppInfo;
import net.tatans.coeus.network.callback.HttpHandler;
import net.tatans.coeus.network.tools.TatansToast;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/10/27.
 */
public class AppUtils {

    public static HashMap<String, HttpHandler<File>> httpHashmap = new HashMap<String, HttpHandler<File>>();

    /**
     * 获取手机已安装应用的信息
     */
    public static ArrayList<LocalAppInfo> getLocalAppInfo(Context context) {
        ArrayList<LocalAppInfo> allAppList = new ArrayList<LocalAppInfo>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        Log.e("PACKAGES'SIZE", String.valueOf(packages.size()));
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            LocalAppInfo tmpInfo = new LocalAppInfo();
            tmpInfo.appName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            tmpInfo.packageName = packageInfo.packageName;
            tmpInfo.versionName = packageInfo.versionName;
            tmpInfo.versionCode = packageInfo.versionCode;
            tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(context.getPackageManager());
            allAppList.add(tmpInfo);
        }
        return allAppList;
    }

    /**
     * 启动安装完成的应用
     *
     * @param context     Context
     * @param packageName 应用包名
     */
    public static void startAppByPackageName(final Context context,
                                             String packageName) {
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(pi.packageName);

            List<ResolveInfo> apps = context.getPackageManager()
                    .queryIntentActivities(resolveIntent, 0);

            ResolveInfo ri = apps.iterator().next();
            if (ri != null) {
                final Intent intent = context.getPackageManager()
                        .getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    context.startActivity(intent);

                } else {
                    Log.e("start_app:", packageName + "应用无法打开");
                }
            } else {
                Log.e("start_app:", "没有找到" + packageName + "应用");
            }
        } catch (Exception e) {
            e.printStackTrace();
            TatansToast.showAndCancel("该应用无法打开请在别处操作");
        }

    }


    /**
     * 通过包名获取应用程序的名称。
     *
     * @param context     Context对象。
     * @param packageName 包名。
     * @return 返回包名所对应的应用程序的名称。
     */
    public static String getAppNameByPackageName(Context context,
                                                 String packageName) {
        PackageManager pm = context.getPackageManager();
        String name = null;
        try {
            name = pm.getApplicationLabel(
                    pm.getApplicationInfo(packageName,
                            PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * 获取手机已安装的应用信息--卸载界面
     */
    public List<LocalAppInfo> getLocalApp(Context context) {
        List<LocalAppInfo> localAppInfoList = new ArrayList<LocalAppInfo>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                LocalAppInfo tmpInfo = new LocalAppInfo();
                tmpInfo.appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                tmpInfo.packageName = packageInfo.packageName;
                tmpInfo.versionName = packageInfo.versionName;
                tmpInfo.versionCode = packageInfo.versionCode;
                tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(pm);
                localAppInfoList.add(tmpInfo);
            }
        }
        return localAppInfoList;
    }

    /**
     * 获取Android Native App的缓存大小、数据大小、应用程序大小
     *
     * @param context Context对象
     * @param pkgName 需要检测的Native App包名
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void getPkgSize(final Context context, final String pkgName) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        java.lang.reflect.Method method = PackageManager.class.getMethod("getPackageSizeInfo", new Class[]{String.class, IPackageStatsObserver.class});
        // 调用 getPackageSizeInfo 方法，需要两个参数：1、需要检测的应用包名；2、回调
        method.invoke(context.getPackageManager(), new Object[]{pkgName, new IPackageStatsObserver.Stub() {
            @Override
            public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                // 从pStats中提取各个所需数据
                long sum = pStats.cacheSize + pStats.dataSize + pStats.codeSize;

            }
        }});
    }

    /**
     * 描述: 普通卸载
     * 修改人: 吴传龙
     * 最后修改时间:2015年3月8日 下午9:07:50
     */
    public static boolean uninstall(String packageName, Context context) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(uninstallIntent);
        return true;
    }

    /**
     * 本地应用信息与应用商店应用信息比较判断是否需要下载更新
     */
    public static List<DownloadInfo> CompareAppUpdate(List<AppInfo.AI> info, List<LocalAppInfo> localAppInfoList) {
        Log.e("CompareApp", "CompareApp");
        List<DownloadInfo> updateList = new ArrayList<DownloadInfo>();
        LocalAppInfo localAppInfo;
        AppInfo.AI appinfo;
        for (int i = 0; i < localAppInfoList.size(); i++) {
            for (int j = 0; j < info.size(); j++) {
                localAppInfo = localAppInfoList.get(i);
                appinfo = info.get(j);
                if (localAppInfo.packageName.equals(appinfo.getPackageName()) && (localAppInfo.versionCode < appinfo.getVersionCode())) {
                    DownloadInfo infos = new DownloadInfo();
                    infos.setApp_name(appinfo.appName);
                    infos.setType(-1);
                    infos.setId(appinfo.getId());
                    infos.setVersionName(appinfo.versionName);
                    infos.setVersionCode(appinfo.getVersionCode());
                    infos.setApp_packageName(appinfo.packageName);
                    infos.setDecription(appinfo.decription);
                    infos.setIcon_uri(appinfo.iconUrl);
                    infos.setApp_size(appinfo.size);
                    infos.setUri(appinfo.url);
                    infos.setDownload_state("未更新");
                    updateList.add(infos);

                }

            }
        }
        return updateList;
    }

}
