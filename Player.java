package com.mingky.spindragon;

import android.graphics.Bitmap;

/**
 * Created by alfo06-10 on 2018-03-29.
 */

public class Player {

    int width, height;

    Bitmap img;

    int x, y;
    int w, h;

    boolean canMove= false;     //움직일 수 있는가?

    Bitmap[][] imgs;

    int kind;       //캐릭터 종류(0:Red, 1:Violet, 2:Black)
    int index= 0;   //이미지 번호(날개짓 번호)

    int loop= 0;

    int angle= 0;   //회전각도
    int da= 3;      //회전각도 변화량

    int HP= 3;

    double radian;  //이동각도
    int speed;      //이동속도



    public Player(int width, int height, Bitmap[][] imgs, int kind) {
        this.width = width;
        this.height = height;
        this.imgs = imgs;
        this.kind = kind;


        img= imgs[kind][index];

        w= img.getWidth()/2;
        h= img.getHeight()/2;

        x= width/2;
        y= height/2;

        //플레이어가 한 번 움직일 때 이동할 거리
        speed= w/4;


    }//생성자


    void move(){

        //날개짓 애니메이션
        loop++;

        if(loop%3==0){
            index++;

            if(index>3) index=0;

            img= imgs[kind][index];

        }


        //회전
        angle+= da;

        //이동(x, y 좌표변경)
        if(canMove){
            x= (int)(x + Math.cos(radian)*speed);   //Math는 결과를 항상 double형으로 보여준다.
//        y= (int)(y + Math.sin(radian*speed));   //int로 형변환!
            y= (int)(y - Math.sin(radian)*speed);   // + 일경우 반대로 움직입니당! 부호를 반대로!

            if(x < w) x= w;
            if(x > width-w) x= width-w;
            if(y < h) y= h;
            if(y > height-h) y= height-h;

        }

    }


}
