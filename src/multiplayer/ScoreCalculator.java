package multiplayer;

public class ScoreCalculator {
    
    public static double normalize(double mine, double theirs) {
        if (mine == 0 && theirs == 0) return 0.5;
        return mine / (mine + theirs);
    }

    public static double calculate(PlayerMatchResult mine, PlayerMatchResult opponent) {
        double myThroughput = mine.timeline.isEmpty() ? 0 : 
            (double) mine.timeline.get(mine.timeline.size() - 1).itemsProduced() / mine.timeline.get(mine.timeline.size() - 1).tick();
        double oppThroughput = opponent.timeline.isEmpty() ? 0 : 
            (double) opponent.timeline.get(opponent.timeline.size() - 1).itemsProduced() / opponent.timeline.get(opponent.timeline.size() - 1).tick();

        double score = 0.35 * normalize(myThroughput, oppThroughput)
                     + 0.25 * normalize(mine.avgEfficiency, opponent.avgEfficiency)
                     + 0.15 * normalize(1.0 / Math.max(mine.footprintTiles, 1), 1.0 / Math.max(opponent.footprintTiles, 1))
                     + 0.25 * normalize(mine.finalCash, opponent.finalCash);
        return score;
    }
}
