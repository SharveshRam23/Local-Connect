package com.example.localconnect.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class ImageUtil {

    // Resize image safely
    public static Bitmap resize(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    // Decode bitmap from path with sampling to avoid OOM
    public static Bitmap decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {
        final android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        android.graphics.BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return android.graphics.BitmapFactory.decodeFile(path, options);
    }

    private static int calculateInSampleSize(android.graphics.BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // Compress and save image
    public static String compressImage(Bitmap bitmap, java.io.File destination) {
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(destination)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out); // 70% quality
            return destination.getAbsolutePath();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
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

    // Rotate image
    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true
        );
    }

    // Convert Bitmap to Base64 String
    public static String toBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] bytes = baos.toByteArray();
        return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
    }

    // Convert Base64 String to Bitmap
    public static Bitmap fromBase64(String base64String) {
        if (base64String == null || base64String.isEmpty()) return null;
        try {
            byte[] decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
            return android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Aggressive compression for Firestore Base64 (Resize to 400px, 60% quality)
    public static String toBase64Aggressive(Bitmap bitmap) {
        if (bitmap == null) return null;
        Bitmap resized = resize(bitmap, 400, 400);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] bytes = baos.toByteArray();
        return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
    }
}
