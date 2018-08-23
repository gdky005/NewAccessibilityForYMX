package com.gdky005.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class WQAccessibilityService extends AccessibilityService {

    public static int count = 0;

    private static final String TAG = "WQAccessibilityService";

    private static final int FLAG_MESSAGE_CLICK_EVENT = 0;
    private static final int FLAG_MESSAGE_INPUT_EVENT = 1;
    private static final int FLAG_MESSAGE_SCROLL_EVENT = 2;
    private static final int FLAG_MESSAGE_REVIEW_EVENT = 3;


    private static final int SEND_DELAY_TIME = 1000;


    private static final String LINEAR_LAYOUT = LinearLayout.class.getCanonicalName();
    private static final String BUTTON = Button.class.getCanonicalName();
    private static final String TEXTVIEW = TextView.class.getCanonicalName();
    private static final String EDITTEXT = EditText.class.getCanonicalName();
    private static final String LISTVIEW = ListView.class.getCanonicalName();


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected()");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind() called with: intent = [" + intent + "]");
        return super.onUnbind(intent);
    }

    /**
     * 通过这个函数可以接收系统发送来的AccessibilityEvent，接收来的AccessibilityEvent是经过过滤的，过滤是在配置工作时设置的。
     * <p>
     * 这是异步通知
     *
     * @param event 事件
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent() event:" + event.getText());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            nodeInfo(event);
        }
    }

    @Override
    public void onInterrupt() {

    }


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            AccessibilityNodeInfo node = (AccessibilityNodeInfo) msg.obj;

            switch (msg.what) {
                case FLAG_MESSAGE_REVIEW_EVENT:
                case FLAG_MESSAGE_CLICK_EVENT:
                    runPerformAction(node, AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                case FLAG_MESSAGE_INPUT_EVENT:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        // TODO: 2016/10/26  可以再次输入内容
                        runPerformAction(node, AccessibilityNodeInfo.ACTION_SET_TEXT, "Android");
                    }
                    break;
                case FLAG_MESSAGE_SCROLL_EVENT:
                    runPerformAction(node, AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                    break;
            }
        }

        /**
         * 执行对应操作 事件
         * @param node
         * @param type
         */
        private void runPerformAction(AccessibilityNodeInfo node, int type) {
            runPerformAction(node, type, "");
        }

        /**
         * 执行对应操作 事件
         * @param node
         * @param type
         */
        private void runPerformAction(AccessibilityNodeInfo node, int type, String text) {
            if (node != null) {
                if (AccessibilityNodeInfo.ACTION_SET_TEXT == type) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 大于等于 5.0 系统 可以给 设置  文本
                        Bundle arguments = new Bundle();
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
                        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                    }
                } else if (AccessibilityNodeInfo.ACTION_SCROLL_FORWARD == type) {
                    AccessibilityNodeInfo nodeInfo = node.getChild(0);
                    if (nodeInfo != null) {
                        boolean b = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                        if (b) {
                            count++;
                            return;
                        }

                        nodeInfo = node.getChild(1).getChild(0);

                        if (nodeInfo != null) {
                            gestureEvent(nodeInfo);
                        }
                    }
                } else {
                    node.performAction(type);
//                    gestureEvent(node);
                }
            }
        }

        /**
         * 手势事件
         * @param node
         */
        private void gestureEvent(AccessibilityNodeInfo node) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Rect rect = new Rect();
                node.getBoundsInScreen(rect);

                Log.d(TAG, "printTree: bound:" + rect);
                Point position = new Point(rect.left + 100, rect.top + 100);
                GestureDescription.Builder builder = new GestureDescription.Builder();
                Path p = new Path();
                p.moveTo(position.x, position.y);

                Log.d(TAG, "Path: x=" + position.x + ", y=" + position.y);

                builder.addStroke(new GestureDescription.StrokeDescription(p, 100L, 50L));

                GestureDescription gesture = builder.build();
                boolean isDispatched = dispatchGesture(gesture, new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        super.onCompleted(gestureDescription);
                        Log.d(TAG, "onCompleted: 完成..........");
                    }

                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        super.onCancelled(gestureDescription);
                        Log.d(TAG, "onCancelled: 取消..........");
                    }
                }, null);

                Log.d(TAG, "runPerformAction:  测试点击的分发数据：" + isDispatched);
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void nodeInfo(AccessibilityEvent event) {
        AccessibilityNodeInfo nodeInfo = event.getSource();
        if (nodeInfo != null) {
            if (getRootInActiveWindow() == null)
                return;

//            test
//            checkName(BUTTON, "com.gdky005.accessibility:id/test_sj_open_btn");

            if (count <= 1) {
                Log.d(TAG, "nodeInfo() called with: event = [" + event + "] count=" + count);

                // TODO: 2016/10/26  这里处理相关事件
                checkName(LINEAR_LAYOUT, "cn.amazon.mShop.android:id/rs_search_plate");
                checkName(EDITTEXT, "cn.amazon.mShop.android:id/rs_search_src_text");
            } else {
                checkName(LISTVIEW, "cn.amazon.mShop.android:id/iss_search_suggestions_list_view");
                checkName(LISTVIEW, "cn.amazon.mShop.android:id/rs_vertical_stack_view");
            }


//            checkName(LISTVIEW, "cn.amazon.mShop.android:id/rs_vertical_stack_view");
//            checkName(EDITTEXT, "cn.amazon.mShop.android:id/rs_search_src_text");
//            checkName(BUTTON, "cn.amazon.mShop.android:id/rs_search_src_text");
//            checkName(BUTTON, "cn.amazon.mShop.android:id/rs_search_plate");
//            checkName(TEXTVIEW, "搜索");
//            checkName(EDITTEXT, "com.tencent.mm:id/fo");
//            checkName(LISTVIEW, "com.tencent.mm:id/bfr");
        }
    }

    /**
     * 检测名字和数据
     * <p>
     * 务必 大于 等于 Android 4.3 （18） 版本
     * <p>
     * 版本 至少要大约 等于   Android 4.1 （16） 版本，才能使用文字查找。
     * 版本 至少要大约 等于   Android 4.3 （18） 版本，才能使用ID 查找。
     * <p>
     * 版本 至少要大约 等于   Android 5.0（21） 版本，才能使用 动态在 EditText 里面 输入文本内容。
     *
     * @param type
     * @param keyWorld
     */
    private void checkName(String type, String keyWorld) {
        //通过文字 找到当前的节点
        List<AccessibilityNodeInfo> nodes = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) { //版本大于等于   Android 4.0 （14） 版本 可以使用 findAccessibilityNodeInfosByText
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) { //版本大于等于   Android 4.1 （16） 版本 可以使用 getRootInActiveWindow
                nodes = getRootInActiveWindow().findAccessibilityNodeInfosByText(keyWorld);
            }
        }

        if (nodes != null && nodes.size() > 0) {
            matchData(type, nodes);
        } else {
            //版本大于等于   Android 4.3 版本 匹配 ID
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) { //版本大于等于   Android 4.3 （18） 版本 可以使用 findAccessibilityNodeInfosByViewId
                nodes = getRootInActiveWindow().findAccessibilityNodeInfosByViewId(keyWorld);
                if (nodes != null && nodes.size() > 0) {
                    matchData(type, nodes);
                }
            }
        }
    }


    /**
     * 匹配数据， 延迟 后发送消息
     *
     * @param type
     * @param nodes
     */
    private void matchData(String type, List<AccessibilityNodeInfo> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            AccessibilityNodeInfo node = nodes.get(i);
            if (node.getClassName().equals(type) && node.isEnabled()) {
                int what = getWhatState(type);
                handler.removeMessages(what);

                Message msg = handler.obtainMessage();
                msg.what = what;
                msg.obj = node;
                handler.sendMessageDelayed(msg, SEND_DELAY_TIME);
            }
        }
    }

    /**
     * 获取状态类型
     *
     * @param type
     * @return
     */
    private int getWhatState(String type) {
        int what;

        if (EDITTEXT.equals(type)) {
            what = FLAG_MESSAGE_INPUT_EVENT;
            count++;
        } else if (LISTVIEW.equals(type)) {
            what = FLAG_MESSAGE_SCROLL_EVENT;
        } else if (TEXTVIEW.equals(type)) {
            what = FLAG_MESSAGE_REVIEW_EVENT;
        } else {
            what = FLAG_MESSAGE_CLICK_EVENT;
        }

        return what;
    }


}
