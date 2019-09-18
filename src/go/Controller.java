/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package go;

import java.awt.Graphics;
import java.awt.*;
import java.util.ArrayList;

/**
 * Controller in Model/View/Controller pattern, acts as go between for model and view
 * Based on levenick code
 * @author tvtuttle
 */
public class Controller extends Thread{
    Board theBoard;
    GenLearner alpha;
    AdvLearner beta;
    GoPanel thePanel;
    int humanPlayer;
    boolean played;
    ArrayList<Integer> pastScores;
    public Controller(){
        theBoard = new Board();
        alpha = new GenLearner();
        beta = new AdvLearner();
    }
    
    public Controller(int goSize){
        theBoard = new Board(goSize);
        alpha = new GenLearner();
        
        beta = new AdvLearner();
       
        alpha.setColor(Board.BLACK);
        beta.setColor(Board.WHITE);
        humanPlayer = Board.WHITE;
//        alpha.setColor(Board.WHITE);
//        humanPlayer=(Board.BLACK);
//        beta.setColor(Board.BLACK);
        theBoard.setController(this);
        pastScores = new ArrayList();
    }
    
    public void paint(Graphics g){
        theBoard.paint(g);
    }
    
    public void run(){
        while (!theBoard.gameOver()){
            if (theBoard.whoseTurn==humanPlayer){
                played = false;
                while (!played){
                    delay(100);
                }
            }
            else {
                makeComputerPlay();
            }
            delay(100);
            thePanel.repaint();
        }
    }
    
    void makeComputerPlay(){
        Move m = null;
        int previous = alpha.getScore(theBoard);
        if (theBoard.whoseTurn == alpha.getColor()){
//            System.out.println("hyah");
            m = alpha.chooseMove(theBoard);
        }
        else {
//            System.out.println("not hyah");
            m = beta.chooseMove(theBoard);
        }
        theBoard.tryMove(m);
        if (theBoard.gameOver()){
            int finalScore = new ScoreCounterSimple(theBoard).countScore();
            if (finalScore > 0){
                alpha.markAsLoss();
                if (alpha.getLosses() > 3){
                    alpha.changeAlgorithm();
                }
            }
            theBoard.resolveGame();
        }
//        int after = alpha.getScore(theBoard);
//        if (previous > after){
//            alpha.incNegativeWeights();
//            alpha.decPositiveWeights();
//        }
//        else if (previous < after){
//            alpha.incPositiveWeights();
//            alpha.decNegativeWeights();
//        }
    }
    
    void delay(int i){
        try {
            sleep(i);
        } catch (Exception e){
            
        }
    }
    
    // attempts to use an advanced scoring polynomial
    // new plan: consider prediction/correction
    // get scorePoly calc at each step. then, at end,
    // backprop the final score and calc diff btw that and the scorepoly
    // this diff is the error, which we use to change values
    public void train(int num){
        // for now, assume that alpha always goes first
        // therefore alpha is black and therefore negative
        reset();
        for (int i = 0; i < num; i ++){
            while (!theBoard.gameOver()){
                Move a = alpha.chooseMove(theBoard);
//                if (a.isPass() && theBoard.passCount == 1){
//                    break;
//                }
//                System.out.println(a);
                theBoard.tryMove(a);
//                System.out.println("hello");
                pastScores.add(alpha.getBestScore());
                /*
                From Samuels:
                Alpha keeps a record of the apparent goodness of its board po- 
                sitions as the game progresses. This record is kept by 
                computing the scoring polynomial for each board position 
                encountered in actual play and by saving this polynomial 
                in its entirety. At the same time, Alpha also com- 
                putes the backed-up score for all board positions, 
                using the look-aheadpro- cedure described earlier. 
                At each play by Alpha the initial board score as saved 
                from the previous Alpha move, is compared with the backed-up 
                score for the current position. The difference between these 
                scores, defined as delta, is used to check the scoring 
                polynomial. If delta is positive it is reasonable to 
                assume that the initial board evaluation was in error 
                and terms which contributed positively should have been given 
                more weight, while those that contributed negatively 
                should have been given less weight! A converse statement 
                can be made for the case where delta is negative.
                */
                if (theBoard.gameOver()){
                    break;
                }
                Move b = beta.chooseMove(theBoard);
//                if (b.isPass() && theBoard.passCount == 1){
//                    break;
//                }
                theBoard.tryMove(b);
//                System.out.println(b);
            }
            // at this point, the game will be over
            // we reach around existing mechanisms to grab the score
            int finalScore = new ScoreCounterSimple(theBoard).countScore();
            // once we have final score, we can backpropagate error based on
            // predicted scores here
//            int level = 1;
//            System.out.println(pastScores.size());
//            for (int j = pastScores.size()-1; j >= 0; j--) {
//                int jthRecentScore = pastScores.get(j);
//                int error = finalScore - jthRecentScore;
////                System.out.println("finalScore = " + finalScore);
////                System.out.println("jthRecentScore = " + jthRecentScore);
//                if (error > 0){
//                    // then score should be proportionally increased
//                    alpha.incPositiveWeights(j);
//                    alpha.decNegativeWeights(j);
//                    System.out.println("weights increased");
//                }
//                else if (error < 0){
//                    alpha.decPositiveWeights(j);
//                    alpha.incNegativeWeights(j);
//                    System.out.println("weights decreased");
//                }
//                // if error=0, do nothing
//            }
            if (finalScore < 0){
                // if alpha wins, give beta its scoring system (disabled for now)
                // instead, adversary just chooses best move
                System.out.println("Alpha!");
//                beta.setScorePoly(alpha.getScorePoly());

            }
            else if (finalScore > 0){
                // if beta wins, give alpha a black mark
                System.out.println("Beta!");
                alpha.markAsLoss();
                if (alpha.getLosses() > 3){
                    alpha.changeAlgorithm();
                }
            }
            else if (finalScore == 0){
                System.out.println("Jigo!");
            }
        }
        alpha.printWeights();
//        thePanel.reset();
        reset();
    }
    
    // only uses Samuel's minimax to optimize score on board
    // in other words, minimax is the only heuristic used
    // only viable at small board sizes
    public void minimax(){
        // or just don't and stop overreacting lmao
    }
    
    public void setPanel(GoPanel p){
        thePanel = p;
    }
    
    public Rectangle getRect(){
        Rectangle r = thePanel.getBounds();
        Insets i = thePanel.getInsets();
        int margin = 100;
        int width = r.width - (i.left + i.right + margin);
        int ht = r.height - (i.bottom + i.top + margin);
        int size = Math.min(width, ht);
        return new Rectangle(margin/2, i.top+margin/2, size, size);
    }
    
    public void handleClicked(int x, int y){
        theBoard.handleClicked(x, y);
//        if (theBoard.whoseTurn == Board.BLACK);
        if (theBoard.gameOver()){
            int finalScore = new ScoreCounterSimple(theBoard).countScore();
            if (finalScore > 0){
                alpha.markAsLoss();
                if (alpha.getLosses() > 3){
                    alpha.changeAlgorithm();
                }
            }
            theBoard.resolveGame();
        }
    }
    public void reset(){
        theBoard.reset();
    }
    public void pass(){
        theBoard.pass();
        if (theBoard.gameOver()){
            int finalScore = new ScoreCounterSimple(theBoard).countScore();
            if (finalScore > 0){
                alpha.markAsLoss();
                if (alpha.getLosses() > 3){
                    alpha.changeAlgorithm();
                }
            }
            theBoard.resolveGame();
        }
    }
}
