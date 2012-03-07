package ca.sfu.message;

import java.io.Serializable;
import java.util.Random;

public class AutomataMsg implements Serializable {

   private static final long serialVersionUID = 1L;

   public int width, height;
   public int bitmap[][];

   public AutomataMsg() {}

   public AutomataMsg(int w, int h)
   {
       width = w;
       height = h;
       bitmap = new int[h][w];
       randomBitmap();
   }
   
   public void nextMoment(boolean left, int[] border)
   {
       int[][] prebitmap = new int[height][width+1];
       int[][] move = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};


       for(int i=0; i<height; i++){
           for(int j=1; j<width+1; j++)
               if(left)
                   prebitmap[i][j-1] = bitmap[i][j-1];
               else
                   prebitmap[i][j] = bitmap[i][j-1];
           if(left)
               prebitmap[i][width] = border[i];
           else
               prebitmap[i][0] = border[i];
       }

       for(int i=0; i<height; i++)
           for(int j=0; j<width; j++)
           {
               int counter = 0;
               for(int k=0; k<8; k++)
               {
                   int x = i + move[k][0], y = j + move[k][1];
                   if(!left)
                       y++;

                   if(x>=0 && x<height && y>=0 && y<width+1 && prebitmap[x][y]==1)
                   {
                       counter ++;
                   }
               }
               if(counter == 3)
               {
                   bitmap[i][j] = 1;
               }else if(counter != 2)
               {
                   bitmap[i][j] = 0;
               }
           }
   }

   public void randomBitmap()
   {
       Random random = new Random();
       for(int i=0; i<height; i++)
           for(int j=0; j<width; j++)
               bitmap[i][j] = random.nextInt(2);
   }

   public void print()
   {
       System.out.println(width + " " + height);
   }

   public int getWidth()
   {
       return width;
   }

   public int getHeight()
   {
       return height;
   }
   
   public int[][] getBitmap()
   {
       return bitmap;
   }

   public void printout(){
       for(int i=0; i<height; i++){
           for(int j=0; j<width; j++)
               System.out.print(bitmap[i][j] + " ");
           System.out.println();
       }
   }

   public AutomataMsg left(){
       AutomataMsg auto1 = new AutomataMsg();
       auto1.width = width/2; //what if width is odd
       auto1.height = height;
       auto1.bitmap = new int[auto1.height][auto1.width];
       for(int i=0; i<auto1.height; i++)
           for(int j=0; j<auto1.width; j++)
               auto1.bitmap[i][j] = bitmap[i][j];
       return auto1;
   }

   public AutomataMsg right(){
       AutomataMsg auto1 = new AutomataMsg();
       auto1.width = width - width/2; //what if width is odd
       auto1.height = height;
       int offset = width/2;

       auto1.bitmap = new int[auto1.height][auto1.width];
       for(int i=0; i<auto1.height; i++)
           for(int j=0; j<auto1.width; j++){
               System.out.println(i+" "+j);
               auto1.bitmap[i][j] = bitmap[i][offset+j];
           }
       return auto1;
   }

   public void mergeLeft(AutomataMsg auto1){
       for(int i=0; i<auto1.height; i++)
           for(int j=0; j<auto1.width; j++)
               bitmap[i][j] = auto1.bitmap[i][j];
   }

   public void mergeRight(AutomataMsg auto1){
       int offset = width/2;
       for(int i=0; i<auto1.height; i++)
           for(int j=0; j<auto1.width; j++)
               bitmap[i][offset+j] = auto1.bitmap[i][j];
   }

}

