package com.trolp.lookupcar;

import java.io.Serializable;

/**
 * Created by leonidas on 6/25/15.
 */
public interface MessageListener extends Serializable {
    void onNewMessage(String title, String desc);
}
