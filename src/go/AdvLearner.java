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
public class AdvLearner extends Learner{
    
    public AdvLearner(){
        color = Board.WHITE;
        scoringSystem = new ScorePoly(1);
    }

    Move chooseMove(Board theBoard) {
        MoveList legalMoves = theBoard.getLegalMoves();
//        Move bestMove = minimax_simple(theBoard);
        Move bestMove = minimax(theBoard, 2);
//        Move bestMove = highScoreMove(legalMoves, theBoard);
//        Move bestMove = legalMoves.get((int)(Math.random()*legalMoves.size()));
        return bestMove;
    }
    
    Move minimax_simple(Board b){
        // note: assumes that all turn stuff is working correctly
        // also assumes that pass is in the legal move list
        // now chooses random move among equal value moves
        MoveList legalMoves = b.getLegalMoves();
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
                if (value > max)
                    max = value;
            }
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
    
    Move minimax(Board b, int n){
        Move bestMove = new Move();
        MoveList bestMoveList = new MoveList();
        int bestScore = Integer.MAX_VALUE;
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
        bestMove = bestMoveList.get((int) (Math.random()*bestMoveList.size()));
        return bestMove;
    }
    // returns the board with the maximum gain
    // this gives the score of the best result for opponent
    // whereas min tries to minimize this value
    int max(Board b, int n){
//        System.out.println("stuck in a rut!");
        if (b.gameOver() || n <= 0){
//            System.out.println("get in here!");
            return scoringSystem.getScore(b);
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
            return scoringSystem.getScore(b);
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
    
    public void setScorePoly(ScorePoly s){
        scoringSystem = s;
        scoringSystem.player = -1*scoringSystem.player;
    }
    
}
