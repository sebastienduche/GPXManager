package com.gpxmanager.component;

public interface IModifyable {

    void reset();

    boolean isModified();

    void setModified(boolean modified);

    void setActive(boolean active);

    void setListenerEnable(boolean listenerEnable);
}
