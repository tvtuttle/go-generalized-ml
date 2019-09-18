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
public class GenLearner extends Learner{
    int losses;
    NodeList previousMoves;
    int bestScore;
//    int previousDelta;

    public GenLearner(){
        losses = 0;
        previousMoves = new NodeList();
        color = Board.BLACK;
        scoringSystem = new ScorePoly(color);
        bestScore = 0;
//        previousDelta = 0;
    }
    public GenLearner(Board b, int col){
        theBoard = b;
        color = col;
        losses = 0;
        previousMoves = new NodeList();
        scoringSystem = new ScorePoly(color);
    }
    
    
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
    
    // uses minimax, which uses scorepoly
    // highest minimax value = best play
    Move chooseMove(Board b) {
        int previous = color*scoringSystem.getScore(b); // the polynomial's score for this position
//        System.out.println(previous);
//        previousMoves.add(new Node(previous, (Board) b.clone())); // a node has been added, consists of this board and its related score
        // now, compute backed up score for this position (i.e. minimax)
        
        int trueScore = minimax(b, 2);// later this should be predetermined, with a higher ply
        int delta = previous - trueScore;
//        if (delta < 0){
//            System.out.println("this happens?");
//        }
//        System.out.println(previous);
        //scoring changes are wack
//        if (delta > 0){
//            scoringSystem.incPositiveWeights(1);
////            scoringSystem.decNegativeWeights(1);
//        }
//        else if (delta < 0){
//            scoringSystem.incNegativeWeights(1);
////            scoringSystem.decPositiveWeights(1);
//        }

        
        // all these printouts are from perspective of the ai, larger is bigger
        
        System.out.println("previousBoardScore: "+ previous);
        System.out.println("trueScore:" + trueScore);
        System.out.println("delta: " + delta);
        scoringSystem.updateWeights(delta);
        //TODO: this is just the beginning, more on this comes after the above quote
        // in samuel
//        System.out.println(b);
        
        MoveList legalMoves = b.getLegalMoves();
//        System.out.println(legalMoves.size());
        Move bestMove = highScoreMove(legalMoves, b);
//        bestMove = legalMoves.get((int) (Math.random()*legalMoves.size()-1));
//        bestMove = minimax_simple(b);
//        bestMove = minimax(b, 2);
//        System.out.println(bestMove);
        return bestMove;
    }
    
    Move highScoreMove(MoveList legal, Board b){
        Move out = new Move();
        int highScore = color*Integer.MAX_VALUE;
        for (int i = 0; i < legal.size(); i ++) {
//            System.out.println(i);
            Board tempBoard = (Board) b.clone();
            tempBoard.tryMove(legal.get(i));
//            System.out.println(tempBoard);
            int value = scoringSystem.getScore(tempBoard);
//            System.out.println(value);
//            System.out.println(value);
            if (value > highScore){
                highScore = value;
                out = legal.get(i);
            }
        }
//        System.out.println(highScore);
        return out;
    }
    
    // the goal of minimax is to minimize the maximum possible gain of the opponent
    // this gain is portrayed as the value of a scoring function
    // in scorepoly
    // this version of minimax is given a board before plays
    // tests all legal moves on board
    // and returns best move, based on the one that minimizes opponent gain
    // this is simple and only goes one level
    Move minimax_simple(Board b){
        // note: assumes that all turn stuff is working correctly
        // also assumes that pass is in the legal move list
        // now chooses random move among equal value moves
        MoveList legalMoves = b.getLegalMoves();
        System.out.println("size="+legalMoves.size());
        // want min here
        int min = Integer.MAX_VALUE;
        int minIndex = 0;
        MoveList minList = new MoveList();
//        System.out.println("trying moves");
        b.setSuppressPrint(true);
        for (int i = 0; i < legalMoves.size(); i++) {
            Board tempBoard = (Board) b.clone(); //inefficient but whatevs
            tempBoard.tryMove(legalMoves.get(i));
//            System.out.println("tempBoard = \n" + tempBoard);
            MoveList legalMovesAdv = tempBoard.getLegalMoves();
//            Board temperBoard = (Board) tempBoard.clone();
            // want max here
            int max = Integer.MIN_VALUE;
//            System.out.println("trying moves");
            for (int j = 0; j < legalMovesAdv.size(); j ++){
                Board temperBoard = (Board) tempBoard.clone();
                temperBoard.tryMove(legalMovesAdv.get(j));
//                System.out.println("temperBoard = \n" + temperBoard);
                int value = scoringSystem.getScore(temperBoard);
//                System.out.println(value);
                if (value > max)
                    max = value;
            }
//            System.out.println("size="+legalMoves.size());
            System.out.println(max);
            if (max < min){
                min = max;
//                minIndex = i;
                minList.clear();
                minList.add(legalMoves.get(i));
            }
            else if (max == min){
                minList.add(legalMoves.get(i));
            }
            
        }
//        System.out.println("end trying moves");
        b.setSuppressPrint(false);
        int index =(int) (Math.random()*minList.size());
        return minList.get(index);
    }
    
    // based on levenick's pseudocode from lecture 7
    // alternates between maximize and minimize
    // since it tries to max one player and min the other
    // TODO: find out why score's not being reported
    // (score reports working for simple minimax)
    // this will be used to get the true score
    // so, we using scorecountersimple, not scorepoly/scoringsystem! (in max/min)
    // used to return a move, now just returns highest backed-up score
    int minimax(Board b, int n){
        Move bestMove = new Move();
        MoveList bestMoveList = new MoveList();
        bestScore = Integer.MAX_VALUE;
        MoveList legalMoves = b.getLegalMoves();
//        System.out.println("break");
        for (Move m : legalMoves) {
            Board tempBoard = (Board) b.clone();
            tempBoard.tryMove(m);
            int value = max(tempBoard, n);
//            System.out.println(value);
            if (value < bestScore){
                bestScore = value;
                bestMove = m;
                bestMoveList.clear();
                bestMoveList.add(m);
            }
            else if (value == bestScore){
                bestMoveList.add(m);
            }
        }
//        bestMove = bestMoveList.get((int) (Math.random()*bestMoveList.size()));
//        return bestMove;
        return bestScore;
    }
    // returns the board with the maximum gain
    // this gives the score of the best result for opponent
    // whereas min tries to minimize this value
    int max(Board b, int n){
//        System.out.println("stuck in a rut!");
        if (b.gameOver() || n <= 0){
//            System.out.println("get in here!");
//            return scoringSystem.getScore(b);
            return new ScoreCounterSimple(b).countScore();
            
        }
        
        MoveList list = b.getLegalMoves();
        int max = Integer.MIN_VALUE;
//        Move bestMove = new Move();
        for (Move move : list) {
            Board tempBoard = (Board) b.clone();
            tempBoard.tryMove(move);
            int value = min(tempBoard, n-1);
            if (value > max){
                max = value;
//                bestMove = move;
            }
        }
        return max;
    }
    
    int min(Board b, int n){
//        System.out.println("stuck in a rut!");
        if (b.gameOver() || n <= 0){
//            System.out.println("get in here!");
//            return scoringSystem.getScore(b);
            return new ScoreCounterSimple(b).countScore();
        }
        
        MoveList list = b.getLegalMoves();
        int min = Integer.MAX_VALUE;
//        Move bestMove = new Move();
        for (Move move : list) {
            Board tempBoard = (Board) b.clone();
            tempBoard.tryMove(move);
            int value = max(tempBoard, n-1);
            if (value < min){
                min = value;
//                bestMove = move;
            }
        }
        return min;
    }

    void markAsLoss() {
        System.out.println("Marked as a loss! If there are 3 losses, the algorithm will change!");
        losses ++;
    }

    int getLosses() {
        return losses;
    }

    void changeAlgorithm() {
        System.out.println("Too many losses! Algorithm Changed.");
        scoringSystem.changeAlgorithm();
        losses = 0;
    }
    
    void incPositiveWeights(int weight){
        scoringSystem.incPositiveWeights(weight);
    }
    void incNegativeWeights(int weight){
        scoringSystem.incNegativeWeights(weight);
    }
    void decPositiveWeights(int weight){
        scoringSystem.decPositiveWeights(weight);
    }
    void decNegativeWeights(int weight){
        scoringSystem.decNegativeWeights(weight);
    }
    
    int getBestScore(){
        return bestScore;
    }
    void printWeights(){
        System.out.println("Printing weights:");
        for (int i = 0; i < scoringSystem.weights.length; i++) {
            System.out.println(scoringSystem.weights[i]);
        }
        System.out.println("done printing weights");
    }
    
}
