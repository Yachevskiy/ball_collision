// Written by Vadim Yachevskiy (slovomatch@gmail.com)
// Jan 2021 under MIT license.

package com.yava.ballapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

public class Main extends Activity {
    Surface surf;
    int screenXsize,
        screenYsize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Point size = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(size);
        screenXsize  = size.x;
        screenYsize  = size.y;

        surf = new Surface(Main.this);
        setContentView(surf);
        surf.resume();
    }

    protected void onResume() {
        super.onResume();
        surf.resume();
    }
    protected void onPause() {
        super.onPause();
        surf.pause();
    }


class Surface extends SurfaceView implements Runnable{
    Thread t=null;
    Canvas c;
    Rect r;
    Paint p;
    SurfaceHolder holder;
    volatile boolean running = false;
    Ball ball1, ball2;
    long lastFrame;
    Boolean collide = false;
    double PI = Math.PI;

    class Ball {
        float x;
        float y;
        float vel;
        double ang;
        int radius;
        float m;

        Ball(float x, float y, float vel, double ang, int radius,float m) {
            this.x = x;
            this.y = y;
            this.vel = vel;
            this.ang = ang;
            this.radius = radius;
            this.m = m;
        }
    }

    public void init() {
         r = new Rect();
         p = new Paint();
         p.setStyle(Paint.Style.FILL);
         ball1 = new Ball( screenXsize/2,screenYsize/2,150,PI/2 + 3*0.017,80,2.0f);
         ball2 = new Ball(screenXsize-50,screenYsize/2,200,3*PI/2 - 3*0.017,50,1.0f);

         lastFrame = System.currentTimeMillis();
    }

    public Surface(Context context) {
        super(context);
        holder = getHolder();
        init();
    }

    @Override
    public void run() {
        long delta;
        while(running) {
            delta = System.currentTimeMillis() - lastFrame; // calculating time elapsed since last frame
            if(!holder.getSurface().isValid()) continue;

            // Calculate ball1
            ball1.x += (ball1.vel*Math.sin(ball1.ang)*delta/1000); // new x-position of ball
            ball1.y -= (ball1.vel*Math.cos(ball1.ang)*delta/1000); // new y-position of ball
            if(ball1.x <= ball1.radius) { //ball collides with left line of screen
                ball1.x = ball1.radius;
                ball1.ang = 2*PI - ball1.ang;
            }
            else if(ball1.x >= screenXsize - ball1.radius) { //ball collides with right line of screen
                ball1.x = screenXsize - ball1.radius;
                ball1.ang = 2*PI - ball1.ang;
            }

            if(ball1.y <= ball1.radius){ //ball collides with top line of screen
                ball1.y = ball1.radius;
                ball1.ang = PI - ball1.ang;
                if(ball1.ang<0) ball1.ang = PI*2 + ball1.ang;
            }
            else if(ball1.y >= screenYsize - ball1.radius) { //ball collides with bottom line of screen
                ball1.y =  screenYsize - ball1.radius;
                ball1.ang = PI - ball1.ang;
                if(ball1.ang<0) ball1.ang = PI*2 + ball1.ang;
            }
            ///

            // Calculate ball2 - the same code as above
            ball2.x += (ball2.vel*Math.sin(ball2.ang)*delta/1000);
            ball2.y -= (ball2.vel*Math.cos(ball2.ang)*delta/1000);

            if(ball2.x <= ball2.radius) {
                ball2.x = ball2.radius;
                ball2.ang = 2*PI - ball2.ang;
            }
            else if(ball2.x >= screenXsize - ball2.radius) {
                ball2.x = screenXsize - ball2.radius;
                ball2.ang = 2*PI - ball2.ang;
            }

            if(ball2.y <= ball2.radius){
                ball2.y = ball2.radius;
                ball2.ang = PI - ball2.ang;
                if(ball2.ang<0) ball2.ang = PI*2 + ball2.ang;
            }
            else if(ball2.y >= screenYsize - ball2.radius) {
                ball2.y =  screenYsize - ball2.radius;
                ball2.ang = PI - ball2.ang;
                if(ball2.ang<0) ball2.ang = PI*2 + ball2.ang;
            }
            ///

            if((ball2.x-ball1.x)*(ball2.x-ball1.x) + (ball2.y-ball1.y)*(ball2.y-ball1.y) <= (ball1.radius+ball2.radius)*(ball1.radius+ball2.radius)) { //ball collision condition
              if(!collide) { // avoid re-calculating of new speed vectors after collision
                  collide = true;
                  double v1x = 0, v2x = 0, v1y = 0, v2y = 0, angToNormal = 0, v1x_new = 0, v2x_new = 0;

                      angToNormal = Math.atan((ball1.x - ball2.x) / (ball1.y - ball2.y));

                      v1x = -ball1.vel*Math.cos(angToNormal+ball1.ang);
                      v1y = -ball1.vel*Math.sin(angToNormal+ball1.ang);
                      v2x = -ball2.vel*Math.cos(angToNormal+ball2.ang);
                      v2y = -ball2.vel*Math.sin(angToNormal+ball2.ang);

                      v1x_new = (v1x*ball1.m + v2x*ball2.m - (v1x - v2x)*ball2.m) / (ball1.m + ball2.m); // equation of momentum conservation law
                      v2x_new = (v1x*ball1.m + v2x*ball2.m - (v2x - v1x)*ball1.m) / (ball1.m + ball2.m); // equation of momentum conservation law

                      if(v1y == 0 && v1x_new == 0) ball1.ang = 0;

                      else if(v1x_new<=0) {
                          ball1.ang = Math.atan(v1y/v1x_new) - angToNormal;
                          if (ball1.ang < 0) ball1.ang += 2 * PI;
                      }
                      else if(v1x_new>=0) {
                          ball1.ang = PI + Math.atan(v1y/v1x_new) - angToNormal;
                      }

                      if(v2y == 0 && v2x_new == 0) ball2.ang = 0;
                      else if(v2x_new<=0) {
                          ball2.ang = Math.atan(v2y/v2x_new) - angToNormal;
                          if (ball2.ang < 0) ball2.ang += 2 * PI;
                      }
                      else if(v2x_new>=0) {
                          ball2.ang = PI + Math.atan(v2y/v2x_new) - angToNormal;
                      }

                  ball1.vel = (float) Math.sqrt(v1x_new * v1x_new + v1y * v1y); //calculate new absolute velocity of ball1
                  ball2.vel = (float) Math.sqrt(v2x_new * v2x_new + v2y * v2y); //calculate new absolute velocity of ball2

              }
            }
            else collide = false;

            lastFrame = System.currentTimeMillis();

            c = holder.lockCanvas();
            if(c!=null) {
                c.drawColor(Color.BLACK); // make all screen black
                p.setColor(Color.BLUE); // first blue ball
                c.drawCircle(ball1.x, ball1.y, ball1.radius, p);
                p.setColor(Color.GREEN); //second green ball
                c.drawCircle(ball2.x, ball2.y, ball2.radius, p);
                holder.unlockCanvasAndPost(c);
            }

        }

    }

    public void pause() {
        running = false;
        while(true) {
            try {
                t.join();
            } catch (InterruptedException e) {
            }
            break;
        }
        t = null;
    }

    public void resume(){
        running = true;
        lastFrame = System.currentTimeMillis(); //freezing time at pause!
        if(!Thread.interrupted()) {
            t = new Thread(this);
            t.start();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent me){
        switch(me.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(running==true) pause(); // pause process
                else resume(); // resume process
                break;
        }
        return true;
    }

}
}
