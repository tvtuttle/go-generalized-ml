/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package go;

import java.util.ArrayList;

/**
 * Contains a scoring polynomial that is continuously edited as part of the learning process
 * This polynomial contains weights for different terms that represent priorities
 * What are these priorities for Go?
 * For now, maybe just consider the score as our only heuristic?
 * @author tvtuttle
 */
public class ScorePoly implements Cloneable{
    
    //TODO: consider a hierarchy of terms: the best term always determines the value
    // of a board, but if 2 boards are equal on a term, the next terms are utilized
    
    // learning is all about prediction and correction
    // the goal of the polynomial is to learn to predict the end
    // of the game based on heuristic values
    // once the game ends, the polynomial should be compared to
    // the final score and difference backpropogated
    int player;
    // priorities
    int PIECE_DIFF, NUM_CAPTURES, CURRENT_SCORE, PASSED,
            NUM_GROUPS, NUM_REGIONS, NUM_CENTER, LARGEST_GROUP,
            NUM_OPP_LIBERTIES;
    int[] terms;
    final int NUM_TERMS = 5;
    // weights
    double[] weights;
    double[] correlations; // for each term,
    int num_moves; //number of moves/adjustments
    int N;
    
    public ScorePoly(){
        
    }
    public ScorePoly(int side){
        player = side;
        initWeights();
        num_moves = 0;
        N = 4;
    }
    
    public int getScore(Board b){
        findTerms(b);
//        initWeights();
        int out = 0;
        for (int i = 0; i < terms.length; i++) {
            out += terms[i]*weights[i];
        }
//        out = (int) (terms[0]*weights[0]);
        return out;
    }
    
    
    // if primary weight isn't enough
    public int getSecondaryScore(Board b){
        findTerms(b);
        int out = 0;
        for (int i = 1; i < NUM_TERMS; i ++){
            out += terms[i]*weights[i];
        }
        return out;
    }
    
    // samuel's scoring algorithm
    // used terms based on heuristics of checkers (wish i knew more)
    // so, ideally, the terms of this will be heuristics of go
    void findTerms(Board theBoard){
//        PIECE_DIFF = theBoard.getNumPieces(player)-theBoard.getNumPieces(-1*player);
        NUM_CAPTURES = numCaptures(theBoard);
        CURRENT_SCORE = player*(new ScoreCounterSimple(theBoard)).countScore(); //special, like samuel's piece adv term
//        PASSED = theBoard.passCount;
//        NUM_GROUPS = theBoard.getNumGroups(player); // 1 is ideal, so this will need to be augmented
//        if (NUM_GROUPS == 0){
//            NUM_GROUPS = 1;
//        }
        LARGEST_GROUP = theBoard.getLargestGroup(player);
        // this can be replaced/supplemented by biggest group size
//        System.out.println("NUM_GROUPS = " + NUM_GROUPS);
//        NUM_REGIONS = theBoard.getNumRegions(player);
        // center squares: center squares are better than edges
        NUM_CENTER = theBoard.getNumCenterPieces(player);
        NUM_OPP_LIBERTIES = theBoard.getLiberties(player*(-1));
//        System.out.println(NUM_CENTER);
        // take if possible
        // look for eyes: fully surrounded - 1 (inc. diagonals)
        int[] t = {CURRENT_SCORE, LARGEST_GROUP, NUM_CENTER,NUM_CAPTURES, NUM_OPP_LIBERTIES}; // consider ordering and signs
//        int[] t = {CURRENT_SCORE, LARGEST_GROUP};
//        int[] t = {CURRENT_SCORE};

        //but like, what if the terms were just board positions?
        
        terms = t;
    }
    
    void initWeights(){
        terms = new int[NUM_TERMS];
        weights = new double[terms.length];
        correlations = new double[NUM_TERMS];
        weights[0] = 10; // first term is significant, like samuel's piece advantage term
        for (int i = 1; i < weights.length; i++) {
//            weights[i] = 1;
            //randomized!
//            weights[i] = (int) (Math.random()*2.0); // either 0 or 1
            weights[i] = 1;
            correlations[i] = 1;
        }
        weights[NUM_TERMS-1] = -1; // speshul
        correlations[NUM_TERMS-1] = -1;
    }
    
    int numCaptures(Board theBoard){
        if (player == Board.BLACK){
            return theBoard.blackCaptures;
        }
        else {
            return theBoard.whiteCaptures;
        }
    }
    
    int getPlayer(){
        return player;
    }
    void setPlayer(int i){
        player = i;
    }
    
    void changeAlgorithm(){
        System.out.println("changing");
        
        // set highest non current_score correlation coeff to half
        int index = getMaxCoefficientIndex();
        correlations[index] = correlations[index]/2;
        // sets first nonzero term to 0 and first zero term to 1 or -1
        // scoring term isn't removed tho
//        int recentlyChanged = -1;
//        for (int i = 1; i < weights.length; i++) {
//            if (weights[i] != 0){
//                recentlyChanged = i;
//                weights[i] = 0;
//                break;
//            }
//        }
//        for (int i = 1; i < weights.length; i ++){
//            if (weights[i] == 0 && i != recentlyChanged){
//                if (correlations[i] >= 0)
//                        weights[i] = 1;
//                else if (correlations[i] < 0){
//                        weights[i] = -1;
//                }
//            }
//        }
//        weights[max] = 0;
    }
    
    public Object clone(){
        ScorePoly scorer = null;
        try{
            scorer = (ScorePoly) super.clone();
        } catch(Exception e){
            System.out.println("Ugh" + e);
        }
        scorer.player = player;
        scorer.terms = new int[terms.length];
        scorer.weights = new double[weights.length];
        for (int i = 0; i < terms.length; i ++){
            scorer.weights[i] = weights[i];
            scorer.terms[i] = terms[i];
        }
        return scorer;
    }
    /*
    A record is kept of the correlation existing between the signs of the in- 
    dividual term contributions in the initial scoring polynomial and the sign
    of delta. After each play an adjustment is made in the values of the cor-
    relation coefficients, due account being taken of the number of times that
    each particular term has been used and has had a nonzero value. The co- 
    efficient for the polynomial term (other than the piece-advantage term) 
    with the then largest correlation coefficient is set at a prescribed maximum
    value with proportionate values determined for all of the remaining co- 
    efficients. Actually, the term coefficients are fixed at integral powers of 2,
    this power being defined by the ratio of the correlation coefficients.
    More precisely, if the ratio of two correlation coefficients is equal to or
    larger than n but less than n+l, where n is an integer, then the ratio of 
    the two term coefficients is set equal to 2^n. This procedure was adopted 
    in order to increase the range in values of the term'coefficients.
    Whenever a correla- tion-coefficient calculation leads to a negative sign,
    a corresponding reversal is made in the sign associated with the term itself.
    */
    void updateWeights(int delta){
        //delta = the error between the game score and the polynomial
        // keep it simple! and now that simple is working, we can mess around more confidently
        
        // more advanced:
        // a record is kept in each weight update of the correlation btw. signs of delta and each weight
        // consider devaluing older correlation compared to newer correlation
        for (int i = 0; i < NUM_TERMS; i++) {
            if (delta > 0 && weights[i] > 0){
                correlations[i] ++; //how often a positive weight is corr w/ a positive delta
                if (weights[i] > 1){
                    correlations[i] ++; //bonus points for the heavyweight
                }
            }
            if (delta < 0 && weights[i] < 0){
                correlations[i] --; // how often a negative weight is corr. w/ a neg delta
            }    
        }
        
                
        // based on the correlation coefficients in correlations,
        // find term with largest correlation coefficient
        // set the weight for that term to a specified maximum
        // and set the weights of all other terms to proportional values
        int max_weight = 5;
        int maxIndex = getMaxCoefficientIndex();
//        double ratioMax = Math.abs(weights[maxIndex]);
        weights[maxIndex] = max_weight;
        for (int i = 1; i < NUM_TERMS; i++) {
            if (i == maxIndex){
                //do nothing
            }
            else {
//                int abs_weight = Math.abs((int) weights[i]);
//                int new_weight = (int) ((ratioMax/max_weight)*abs_weight);
//                weights[i] = new_weight;
                weights[i] = 1;
            }
            if (weights[i] > 0 && correlations[i] < 0){
                weights[i] = -1*weights[i];
            }
            if (weights[i] < 0 && correlations[i] > 0){
                weights[i] = -1*weights[i];
            }
        }

        for (int i = 0; i < NUM_TERMS; i++) {
            System.out.println("w"+i+": "+ weights[i]);
        }
        for (int i = 0; i < NUM_TERMS; i++) {
            System.out.println("corr"+i+": "+ correlations[i]);
        }
    }
    
    int getMaxCoefficientIndex(){
        int out = 0;
        double max = 0;
        ArrayList<Integer> outList = new ArrayList();
        for (int i = 1; i < NUM_TERMS; i++) {
            if (Math.abs(correlations[i]) > max){
                outList.clear();
                max = Math.abs(correlations[i]);
                out = i;
                outList.add(i);
            }
            else if (Math.abs(correlations[i]) == max){
                outList.add(i);
            }
        }
//        return out;
        return outList.get((int) (Math.random()*outList.size()));
    }
    
    void incRandomWeight(int n){
        int index = (int) (Math.random()*weights.length);
        weights[index] = weights[index] + n;
        if (correlations[index] < 0 && weights[index] >= 0){
            weights[index] = -1;
        }
    }
    
    void decRandomWeight(int n){
        int index = (int) (Math.random()*weights.length);
        weights[index] = weights[index]-n;
        if (correlations[index] > 0 && weights[index] <= 0){
            weights[index] = 1;
        }
    }

    void incPositiveWeights(double w) {
        for (int i = 0; i < weights.length; i++) {
            if (weights[i]>=0){
                weights[i] = (1/w)*weights[i] + 1;
            }
        }
    }

    void incNegativeWeights(double w) {
        for (int i = 0; i < weights.length; i++) {
            if (weights[i]<=0){
                weights[i] = (1/w)*weights[i] - 1;
            }
        }
    }

    void decPositiveWeights(double w) {
        for (int i = 0; i < weights.length; i++) {
            if (weights[i]>=0){
                weights[i] = (1/w)*weights[i] - 1;
            }
        }
    }

    void decNegativeWeights(double w) {
        for (int i = 0; i < weights.length; i++) {
            if (weights[i]<=0){
                weights[i] = (1/w)*weights[i] + 1;
            }
        }
    }
    
}
