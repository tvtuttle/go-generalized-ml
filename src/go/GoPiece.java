/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package go;
import java.awt.*;
/**
 *
 * @author tvtuttle
 */
public class GoPiece {
    int x = 100;
    int y = 100;
    int r = 50; //radius value
    Color c;
    
    GoPiece(int x, int y, int r, Color c){
        this.x = x;
        this.y = y;
        this.r = r;
        this.c = c;
    }
    
    void setX(int nu){
        x = nu;
    }
    void setY(int nu){
        y = nu;
    }
    void setR(int nu){
        r = nu;
    }
    int getX(){
        return x;
    }
    int getY(){
        return y;
    }
    int getR(){
        return r;
    }
    public void paint(Graphics g){
        g.setColor(c);
        g.fillOval(x-r, y-r, r*2, 2*r);
    }
}
