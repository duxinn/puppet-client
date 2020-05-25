package com.mango.puppet.plugin;

public class PluginModel {
    private String packageName;
    private String activityName;
    private String dexPath;
    private String dexVersion;
    private boolean isRun = false;

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getDexVersion() {
        return dexVersion;
    }

    public void setDexVersion(String dexVersion) {
        this.dexVersion = dexVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDexPath() {
        return dexPath;
    }

    public void setDexPath(String dexPath) {
        this.dexPath = dexPath;
    }

    public boolean isRun() {
        return isRun;
    }

    public void setRun(boolean run) {
        isRun = run;
    }
}
