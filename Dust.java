package com.mingky.spindragon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

/**
 * Created by alfo06-10 on 2018-03-30.
 */

public class Dust {

    Bitmap[] img;

    int[] x= new int[6];    //기존의 x, y, w, h가 6개씩 있는거라고 생각하면 된다!
    int[] y= new int[6];
    int[] rad= new int[6];

    double[] radian= new double[6];
    int[] speed= new int[6];

    boolean isDead= false;  //이 친구는 한번에 6개를 다 없애는 것이므로 배열이 필요하지 않다.
    int life= 30;


    public Dust(Bitmap[] img, int ex, int ey) {
        this.img = img;

        Random rnd= new Random();


        for(int i=0; i<6; i++){
            x[i]= ex;
            y[i]= ey;
            //시작점은 모두가 똑같다!

            rad[i]= img[i].getWidth()/2;
            //서로 다른 6개의 값을 받아온다!

            int angle= rnd.nextInt(360);
            radian[i]= Math.toRadians(angle);

            speed[i]= rad[i]/8;

        }

    }


    void move(){

        for(int i=0; i<6; i++){
            x[i]= (int)(x[i]+Math.cos(radian[i])*speed[i]);
            y[i]= (int)(y[i]-Math.sin(radian[i])*speed[i]);
        }

        life--;

        if(life<=0)   isDead= true;


    }

}
