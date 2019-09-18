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
public abstract class Learner {
    ScorePoly scoringSystem;
    int color;
    Board theBoard;
    public ScorePoly getScorePoly(){
        return scoringSystem;
    }
    public void setScorePoly(ScorePoly s){
        scoringSystem = s;
    }
    public void setColor(int c){
        color = c;
    }
    public int getColor(){
        return color;
    }
    public int getScore(Board b){
        return scoringSystem.getScore(b);
    }
    
    abstract Move chooseMove(Board theBoard);
}
