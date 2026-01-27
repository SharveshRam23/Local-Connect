package com.example.localconnect.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class ImageUtil {

    // Resize image
    public static Bitmap resize(Bitmap bitmap, int width, int height) {

        return Bitmap.createScaledBitmap(
                bitmap,
                width,
                height,
                true
        );
    }

    // Convert to grayscale
    public static Bitmap toGrayScale(Bitmap bitmap) {

        Bitmap grayBitmap = Bitmap.createBitmap(
                bitmap.getWidth(),
                bitmap.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(grayBitmap);

        Paint paint = new Paint();

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        paint.setColorFilter(
                new ColorMatrixColorFilter(matrix)
        );

        canvas.drawBitmap(bitmap, 0, 0, paint);

        return grayBitmap;
    }
}
