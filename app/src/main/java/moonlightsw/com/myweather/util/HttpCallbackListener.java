package moonlightsw.com.myweather.util;

/**
 * Created by MoonlightSW on 2016/3/14.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
