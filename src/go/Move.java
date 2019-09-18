/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package go;

/**
 *
 * @author tvtuttle
 */
public class Move {
    int x,y;
    public Move(){
        x = -1;
        y = -1;
    }
    public Move(int a, int b){
        x = a;
        y = b;
    }
    
    int getX(){
        return x;
    }
    int getY(){
        return y;
    }
    boolean isPass(){
        return (x==-1&&y==-1);
    }
    
    @Override
    public String toString(){
        return "Move at location x="+x+", y="+y;
    }
}
