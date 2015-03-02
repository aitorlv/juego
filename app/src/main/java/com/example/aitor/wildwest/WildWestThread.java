package com.example.aitor.wildwest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.Random;

/**
 * Created by aitor on 28/02/2015.
 */
public class WildWestThread extends Thread{
    private Context myContext;
    private SurfaceHolder mySurfaceHolder;
    private Bitmap backgroundImg;
    private int backgroundOrigW;
    private int backgroundOrigH;
    private float scaleW;
    private float scaleH;
    private float drawScaleW;
    private float drawScaleH;
    private Bitmap mask;
    private Bitmap bandit;
    private int bandit1x, bandit2x, bandit3x, bandit4x, bandit5x;
    private int bandit1y, bandit2y, bandit3y, bandir4y, bandit5y;
    private int screenW = 1;
    private int screenH = 1;
    private boolean running = false;
    private boolean onTitle = true;
    private int activeMole = 0;
    private boolean moleRising = true;
    private boolean moleSinking = false;
    private int moleRate = 5;
    private int fingerX, fingerY;
    private static SoundPool sounds;
    private static int whackSound;
    private static int missSound;
    private boolean moleJustHit = false;
    private Bitmap whack;
    private boolean whacking = false;
    private int banditShoot = 0;
    private int banditMissed = 0;
    private Paint blackPaint;
    public boolean soundOn = true;
    private boolean gameOver = false;
    private Bitmap gameOverDialog;



    public WildWestThread(SurfaceHolder surfaceHolder, Context context) {
        mySurfaceHolder = surfaceHolder;
        myContext = context;
        backgroundImg = BitmapFactory.decodeResource(context.getResources(), R.drawable.titulo);
        backgroundOrigW = backgroundImg.getWidth();
        backgroundOrigH = backgroundImg.getHeight();
        sounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        whackSound = sounds.load(myContext, R.raw.whack, 1);
        missSound = sounds.load(myContext, R.raw.miss, 1);
    }

    @Override
    public void run() {
        while (running) {
            Canvas c = null;
            try {
                c = mySurfaceHolder.lockCanvas(null);
                synchronized (mySurfaceHolder) {
                    if (!gameOver) {
                        animatebandit();
                    }
                    draw(c);
                }
            } finally {
                if (c != null) {
                   mySurfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
    }

    private void draw(Canvas canvas) {
        try {
            canvas.drawBitmap(backgroundImg, 0, 0, null);
            if (!onTitle) {
                canvas.drawText("Bandit: " + Integer.toString(banditShoot), 10, blackPaint.getTextSize()+10, blackPaint);
                canvas.drawText("Missed: " + Integer.toString(banditMissed), screenW-(int)(200*drawScaleW), blackPaint.getTextSize()+10, blackPaint);
                canvas.drawBitmap(bandit, bandit1x, bandit1y, null);
                canvas.drawBitmap(bandit, bandit2x, bandit2y, null);
                canvas.drawBitmap(bandit, bandit3x, bandit3y, null);
                canvas.drawBitmap(bandit, bandit4x, bandir4y, null);
                canvas.drawBitmap(bandit, bandit5x, bandit5y, null);

                //AQUI SE DIBUJA LA MASCARA

                canvas.drawBitmap(mask, (int) 530*drawScaleW, (int) 220*drawScaleH, null);
            }
            if (whacking) {
                canvas.drawBitmap(whack, fingerX - (whack.getWidth()/2), fingerY - (whack.getHeight()/2), null);
            }
            if (gameOver) {
                canvas.drawBitmap(gameOverDialog, (screenW/2) - (gameOverDialog.getWidth()/2), (screenH/2) - (gameOverDialog.getHeight()/2), null);
            }
        } catch (Exception e) {
        }
    }

    boolean doTouchEvent(MotionEvent event) {
        synchronized (mySurfaceHolder) {
            int eventaction = event.getAction();
            int X = (int)event.getX();
            int Y = (int)event.getY();

            switch (eventaction ) {

                case MotionEvent.ACTION_DOWN:
                    if (!gameOver) {
                        fingerX = X;
                        fingerY = Y;
                        if (!onTitle && detectBanditContact()) {
                            whacking = true;
                            if (soundOn) {
                                AudioManager audioManager = (AudioManager) myContext.getSystemService(Context.AUDIO_SERVICE);
                                float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                sounds.play(whackSound, volume, volume, 1, 0, 1);
                            }
                            banditShoot++;
                        }
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    break;

                case MotionEvent.ACTION_UP:
                    if (onTitle) {
                        backgroundImg = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.salon);
                        backgroundImg = Bitmap.createScaledBitmap(backgroundImg, screenW, screenH, true);
                        mask = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.mascara);
                        bandit = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.bandolero);
                        whack = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.whack);
                        gameOverDialog = BitmapFactory.decodeResource(myContext.getResources(), R.drawable.gameover);
                        scaleW = (float) screenW/ (float) backgroundOrigW;
                        scaleH = (float) screenH/ (float) backgroundOrigH;
                        mask = Bitmap.createScaledBitmap(mask, (int)(mask.getWidth()*scaleW), (int)(mask.getHeight()*scaleH), true);
                        bandit = Bitmap.createScaledBitmap(bandit, (int)(bandit.getWidth()*scaleW), (int)(bandit.getHeight()*scaleH), true);
                        whack = Bitmap.createScaledBitmap(whack, (int)(whack.getWidth()*scaleW), (int)(whack.getHeight()*scaleH), true);
                        gameOverDialog = Bitmap.createScaledBitmap(gameOverDialog, (int)(gameOverDialog.getWidth()*scaleW), (int)(gameOverDialog.getHeight()*scaleH), true);
                        onTitle = false;
                        pickActiveBandit();
                    }
                    whacking = false;
                    if (gameOver) {
                        banditShoot = 0;
                        banditMissed = 0;
                        activeMole = 0;
                        pickActiveBandit();
                        gameOver = false;
                    }
                    break;
            }
        }
        return true;
    }

    public void setSurfaceSize(int width, int height) {
        synchronized (mySurfaceHolder) {
            screenW = width;
            screenH = height;
            backgroundImg = Bitmap.createScaledBitmap(backgroundImg, width, height, true);
            drawScaleW = (float) screenW / 1920;
            drawScaleH = (float) screenH / 1080;
            bandit1x = (int) (950*drawScaleW);
            bandit2x = (int) (950*drawScaleW);
            bandit3x = (int) (950*drawScaleW);
            bandit4x = (int) (950*drawScaleW);
            bandit5x = (int) (950*drawScaleW);
            bandit1y = (int) (300*drawScaleH);
            bandit2y = (int) (520*drawScaleH);
            bandit3y = (int) (520*drawScaleH);
            bandir4y = (int) (740*drawScaleH);
            bandit5y = (int) (740*drawScaleH);
            blackPaint = new Paint();
            blackPaint.setAntiAlias(true);
            blackPaint.setColor(Color.BLACK);
            blackPaint.setStyle(Paint.Style.STROKE);
            blackPaint.setTextAlign(Paint.Align.LEFT);
            blackPaint.setTextSize(drawScaleW*30);
        }
    }

    public void setRunning(boolean b) {
        running = b;
    }

   private void animatebandit() {
       if (activeMole == 1) {
           if (moleRising) {
               bandit1y -= moleRate;
           } else if (moleSinking) {
               bandit1y += moleRate;
           }
           if (bandit1y >= (int) (300 * drawScaleH) || moleJustHit) {
               bandit1y = (int) (300 * drawScaleH);
               pickActiveBandit();
           }
           if (bandit1y <= (int) (50 * drawScaleH)) {
               bandit1y = (int) (50 * drawScaleH);
               moleRising = false;
               moleSinking = true;
           }
       }

        if (activeMole == 2) {
            if (moleRising) {
                bandit2x -= moleRate;
            } else if (moleSinking) {
                bandit2x += moleRate;
            }
            if (bandit2x >= (int) (950*drawScaleW) || moleJustHit) {
                bandit2x = (int) (950*drawScaleW);
                pickActiveBandit();
            }
            if (bandit2x <= (int) (600*drawScaleW)) {
                bandit2x = (int) (600*drawScaleW);
                moleRising = false;
                moleSinking = true;
            }
        }
        if (activeMole == 3) {
            if (moleRising) {
                bandit3x += moleRate;
            } else if (moleSinking) {
                bandit3x -= moleRate;
            }
            if (bandit3x >= (int) (1350*drawScaleW) ) {
                bandit3x = (int) (1350*drawScaleW);
                moleRising = false;
                moleSinking = true;
            }
            if (bandit3x <= (int) (950*drawScaleW)|| moleJustHit) {
                bandit3x = (int) (950*drawScaleW);
                pickActiveBandit();
            }
        }
        if (activeMole == 4) {
            if (moleRising) {
                bandit4x -= moleRate;
            } else if (moleSinking) {
                bandit4x += moleRate;
            }
            if (bandit4x >= (int) (950*drawScaleW) || moleJustHit) {
                bandit4x = (int) (950*drawScaleW);
                pickActiveBandit();
            }
            if (bandit4x <= (int) (600*drawScaleW)) {
                bandit4x = (int) (600*drawScaleW);
                moleRising = false;
                moleSinking = true;
            }
        }
        if (activeMole == 5) {
            if (moleRising) {
                bandit5x += moleRate;
            } else if (moleSinking) {
                bandit5x -= moleRate;
            }
            if (bandit5x >= (int) (1350*drawScaleW) ) {
                bandit5x = (int) (1350*drawScaleW);
                moleRising = false;
                moleSinking = true;
            }
            if (bandit5x <= (int) (950*drawScaleW)|| moleJustHit) {
                bandit5x = (int) (950*drawScaleW);
                pickActiveBandit();
            }
        }

    }

    private void pickActiveBandit() {
        if (!moleJustHit && activeMole > 0) {
            if (soundOn) {
                AudioManager audioManager = (AudioManager) myContext.getSystemService(Context.AUDIO_SERVICE);
                float volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                sounds.play(missSound, volume, volume, 1, 0, 1);
            }
            banditMissed++;
            if (banditMissed > 4) {
                gameOver = true;
            }
        }
        activeMole =new Random().nextInt(5) + 1;
        moleRising = true;
        moleSinking = false;
        moleJustHit = false;
        moleRate = 5 + (int)(banditShoot /10);
    }

    private boolean detectBanditContact() {
        boolean contact = false;
        Log.v("entro", fingerX+" "+fingerY);
        Log.v("valor",1350*drawScaleW+"");
        if (activeMole==1 &&
                fingerX >= bandit1x &&
                fingerX < bandit1x +(int)(200*drawScaleW) &&
                fingerY > bandit1y &&
                fingerY < (int) 300*drawScaleH) {
            contact = true;
            moleJustHit = true;
        }
        if (activeMole==2 &&
                fingerX >= bandit2x &&
                fingerX < (int) 950*drawScaleW &&
                fingerY > bandit2y &&
                fingerY < bandit2y +(int)(113*drawScaleH)) {
            contact = true;
            moleJustHit = true;
        }
        if (activeMole==3 &&
                fingerX >= bandit3x &&
                fingerX < (int) (1350*drawScaleW)+ bandit.getWidth() &&
                fingerY > bandit3y &&
                fingerY < bandit3y +(int)(113*drawScaleH)) {
            Log.v("valor",1350*drawScaleW+"");
            contact = true;
            moleJustHit = true;
        }
        if (activeMole == 4 &&
                fingerX >= bandit4x &&
                fingerX < (int) 950*drawScaleW  &&
                fingerY > bandir4y &&
                fingerY < bandir4y +(int)(113*drawScaleH)) {
            contact = true;
            moleJustHit = true;
        }
        if (activeMole == 5 &&
                fingerX >= bandit5x &&
                fingerX < (int) (1350*drawScaleW)+ bandit.getWidth() &&
                fingerY > bandit5y &&
                fingerY < bandit5y +(int)(113*drawScaleH)) {
            contact = true;
            moleJustHit = true;
        }

        return contact;
    }



}
