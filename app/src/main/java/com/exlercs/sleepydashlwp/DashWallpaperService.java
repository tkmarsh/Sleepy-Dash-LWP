package com.exlercs.sleepydashlwp;

import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class DashWallpaperService extends WallpaperService {
    static final String TAG = "gifService";
    static final Handler gifHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public Engine onCreateEngine() {
        try {
            return new GifEngine();
        } catch (IOException e) {
            Log.w(TAG, "Error creating engine", e);
            stopSelf();
            return null;
        }
    }

    class GifEngine extends Engine {
        private final Movie gif;
        private final int duration;
        private final Runnable runnable;
        InputStream is;
        int resWidth;
        int resHeight;
        int imgPath;
        boolean hdpiScreen;
        int when;
        long start;

        GifEngine() throws IOException {
            imgPath=(R.raw.dash_1080);
            is = getResources().openRawResource(imgPath);
            if (is == null) {
                throw new IOException("Unable to open image");
            }

            try {
                gif = Movie.decodeStream(is);
                duration = gif.duration();
            } finally {
                is.close();
            }

            when = -1;
            runnable = new Runnable() {
                @Override
                public void run() {
                    animateGif();
                }
            };
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            gifHandler.removeCallbacks(runnable);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                animateGif();
            } else {
                gifHandler.removeCallbacks(runnable);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            resHeight=height;
            resWidth=width;
            animateGif();
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                                     float xOffsetStep, float yOffsetStep,
                                     int xPixelOffset, int yPixelOffset) {
            super.onOffsetsChanged(
                    xOffset, yOffset,
                    xOffsetStep, yOffsetStep,
                    xPixelOffset, yPixelOffset);
            animateGif();
        }

        void animateGif() {
            tick();

            SurfaceHolder surfaceHolder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = surfaceHolder.lockCanvas();

                if (canvas != null) {
                    gifCanvas(canvas);
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

            gifHandler.removeCallbacks(runnable);

            if (isVisible()) {
                gifHandler.postDelayed(runnable, 1000L/25L);
            }
        }

        void tick() {
            if (when == -1L) {
                when = 0;
                start = SystemClock.uptimeMillis();
            } else {
                long diff = SystemClock.uptimeMillis() - start;
                when = (int) (diff % duration);
            }

        }

        void gifCanvas(Canvas canvas) {
            canvas.save();
            canvas.scale(1f, 1f);
            canvas.drawARGB(255,14,47,80);
            gif.setTime(when);
            gif.draw(canvas, (resWidth-1080)/2, (resHeight-730)/2);
            canvas.restore();
        }
    }
}
