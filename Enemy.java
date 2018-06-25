package com.mingky.spindragon;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by alfo06-10 on 2018-03-30.
 */


//적군도 화면밖에서 나오고, 화면밖으로 나갈때 지워버리자!
public class Enemy {

    int width, height;

    Bitmap img;

    int x, y;
    int w, h;

    boolean isDead= false;  //죽었는가?
    boolean isOut= false;   //화면 밖으로 나갔는가?
    //위 둘을 왜 나눠놨는가? ---→ 점수때문에! isDead : 점수, isOut : 그냥 제거
    boolean wasShow= false; //화면에 보여진 적이 있는가?

    Rect rect;      //화면 사이즈 사각형 좌표 관리 객체

    Bitmap[] imgs;  //왜 1차원 배열? ---→ 한 번 화면으로 나오고나서 그림이 변할 일이 없으므로

    int index;      //날개짓이미지 번호

    int loop= 0;

    int kind;       //종류

    double radian;  //이동각도
    int speed;      //이동속도
    int angle;      //이미지를 회전하는 각도

    int HP;         //체력

    Bitmap[] imgGs;
    Bitmap imgG;



    public Enemy(int width, int height, Bitmap[][] imgSrc, int px, int py, Bitmap[][] imgGauge) {
        this.width = width;     this.height = height;

        Random rnd= new Random();
        //종류 : 0 -→ white, 1 -→ yellow, 2 -→ pink

        // 5:3:2의 비율로 생성되게 하자!
        int n= rnd.nextInt(10); //0~9
        kind= n<5? 0: n<8? 1:2;    //삼항연산자의 연속사용

        //HP(체력) : white -→ 1방, yellow -→ 5방, pink -→ 3방
        HP= kind==0? 1 : kind==1? 5 : 3;

        imgs= imgSrc[kind];
        img= imgs[index];

        w= img.getWidth()/2;
        h= img.getHeight()/2;

        int a= rnd.nextInt(360 );   //0도~365도
        double r= Math.toRadians(a);
        x= (int)(width/2 + Math.cos(r) * width);
        y= (int)(height/2 - Math.sin(r) * width);

        radian= Math.atan2(y-py, px-x);     //원래는 py-y가 맞는거지만, y는 음양수가 반대이기 때문에 반대로 뺀다!
        angle= (int)(270-Math.toDegrees(radian));

        speed= kind==0? w/4: kind==1? w/7: w/10;    //적군들의 속도


        //화면 사이즈 사각형 객체 생성
        rect= new Rect(0, 0, width, height);

        if (kind>0){
            imgGs= imgGauge[kind-1];
            imgG= imgGs[0];

        }

    }

    void damaged(int n/*데미지의 양*/){
        HP-=n;
        if (HP<=0) {
            isDead= true;
            return;

        }
        if (imgG!=null){

            imgG= imgGs[imgGs.length-HP];   //imgs.length도 가능!
        }


    }


    void move(int px, int py){

        //pink인 적군은 각도를 다시 계산
        if(kind==2){
            radian= Math.atan2(y-py, px-x);
            angle= (int)(270- Math.toDegrees(radian));
        }

        //날개짓 애니메이션
        loop++;
        if(loop%3==0){  //3번에 한번씩
            index++;
            if(index>3) index=0;

            img= imgs[index];

        }

        //이동
        x= (int)(x+ Math.cos(radian)*speed);
        y= (int)(y- Math.sin(radian)*speed);    //부호를 반대로~

        if(rect.contains(x, y))  wasShow= true;

        //화면에 한번이라도 보인 적이 있는가?
        if(wasShow){

            //화면 밖으로 나갔는지 확인
            if(x<-w || x>width+w || y<-h || y>height+h){
                isOut= true;

            }


        }




    }



}