package com.kakakoi.wifibell.databinding.adapter;

import androidx.annotation.RawRes;
import androidx.databinding.BindingAdapter;
import androidx.databinding.BindingMethod;
import androidx.databinding.BindingMethods;

import com.airbnb.lottie.LottieAnimationView;

@BindingMethods({
        @BindingMethod(type = com.airbnb.lottie.LottieAnimationView.class, attribute = "lottie_rawRes", method = "setAnimation"),
})
public class LottieAnimationViewBindingAdapter {

    @BindingAdapter("lottie_rawRes")
    public static void setAnimation(LottieAnimationView view, @RawRes final int rawRes) {
        view.setAnimation(rawRes);
        view.playAnimation();
    }
}
